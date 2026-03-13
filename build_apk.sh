#!/bin/bash
set -e

# ============================================================
# Manual Android APK Build Script
# Uses only: android.jar + kotlin-stdlib (both accessible)
# ============================================================

PLATFORM=/usr/lib/android-sdk/platforms/android-34
BUILD_TOOLS=/usr/lib/android-sdk/build-tools/29.0.3
KOTLINC=/opt/kotlinc/bin/kotlinc
D8_JAR=$BUILD_TOOLS/lib/d8.jar
AAPT2=$BUILD_TOOLS/aapt2
APKSIGNER=$BUILD_TOOLS/apksigner
ZIPALIGN=$BUILD_TOOLS/zipalign

SRC_DIR=/home/user/granite/app/src/main
BUILD_DIR=/tmp/apk_build
LIBS_DIR=$BUILD_DIR/libs
CLASSES_DIR=$BUILD_DIR/classes
GEN_DIR=$BUILD_DIR/gen
FLAT_DIR=$BUILD_DIR/flat
OUT_DIR=$BUILD_DIR/out
MAVEN="https://repo1.maven.org/maven2"

echo "==> Cleaning build dir..."
rm -rf $BUILD_DIR
mkdir -p $LIBS_DIR $CLASSES_DIR $GEN_DIR $FLAT_DIR/app $OUT_DIR

# ============================================================
# STEP 1: Download Kotlin stdlib (only dependency needed)
# ============================================================
echo "==> Downloading Kotlin stdlib..."
for jar in kotlin-stdlib kotlin-stdlib-jdk7 kotlin-stdlib-jdk8; do
  dest=$LIBS_DIR/${jar}.jar
  if [ ! -f "$dest" ]; then
    curl -sL --max-time 120 "$MAVEN/org/jetbrains/kotlin/$jar/1.9.22/$jar-1.9.22.jar" -o "$dest"
  fi
done

JARS_FOR_COMPILE="$LIBS_DIR/kotlin-stdlib.jar:$LIBS_DIR/kotlin-stdlib-jdk7.jar:$LIBS_DIR/kotlin-stdlib-jdk8.jar"

# ============================================================
# STEP 2: Compile app resources
# ============================================================
echo "==> Compiling resources with aapt2..."
$AAPT2 compile --dir $SRC_DIR/res -o $FLAT_DIR/app/

echo "==> Linking resources..."
$AAPT2 link \
  --manifest $SRC_DIR/AndroidManifest.xml \
  -I $PLATFORM/android.jar \
  --java $GEN_DIR \
  --output-text-symbols $BUILD_DIR/R.txt \
  -o $OUT_DIR/resources.apk \
  --auto-add-overlay \
  $FLAT_DIR/app/*.flat

echo "==> Generated R.java:"
find $GEN_DIR -name "*.java" | head -5

# ============================================================
# STEP 3: Compile Kotlin + R.java
# ============================================================
echo "==> Compiling Kotlin sources..."
KOTLIN_SOURCES=$(find $SRC_DIR/java -name "*.kt" | tr '\n' ' ')
JAVA_SOURCES=$(find $GEN_DIR -name "*.java" | tr '\n' ' ')

$KOTLINC \
  $KOTLIN_SOURCES \
  $JAVA_SOURCES \
  -classpath "$PLATFORM/android.jar:$JARS_FOR_COMPILE" \
  -d $CLASSES_DIR/app.jar \
  -jvm-target 1.8

# ============================================================
# STEP 4: DEX with D8
# ============================================================
echo "==> Converting to DEX with D8..."
java -cp $D8_JAR com.android.tools.r8.D8 \
  --output $OUT_DIR \
  --lib $PLATFORM/android.jar \
  --min-api 21 \
  $CLASSES_DIR/app.jar \
  $LIBS_DIR/kotlin-stdlib.jar \
  $LIBS_DIR/kotlin-stdlib-jdk7.jar \
  $LIBS_DIR/kotlin-stdlib-jdk8.jar

# ============================================================
# STEP 5: Package APK
# ============================================================
echo "==> Packaging APK..."
APK_UNALIGNED=$OUT_DIR/app-unaligned.apk
cp $OUT_DIR/resources.apk $APK_UNALIGNED
cd $OUT_DIR && zip -j $APK_UNALIGNED classes.dex && cd -

# ============================================================
# STEP 6: Sign APK
# ============================================================
echo "==> Creating debug keystore..."
KEYSTORE=$BUILD_DIR/debug.keystore
keytool -genkey -v \
  -keystore $KEYSTORE \
  -alias androiddebugkey \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass android -keypass android \
  -dname "CN=Android Debug,O=Android,C=US" 2>&1 | grep -E "Generating|Storing|keytool" | head -3

echo "==> Aligning..."
APK_ALIGNED=$OUT_DIR/app-aligned.apk
$ZIPALIGN -f 4 $APK_UNALIGNED $APK_ALIGNED

echo "==> Signing..."
APK_SIGNED=/home/user/granite/app-debug.apk
$APKSIGNER sign \
  --ks $KEYSTORE \
  --ks-key-alias androiddebugkey \
  --ks-pass pass:android \
  --key-pass pass:android \
  --out $APK_SIGNED \
  $APK_ALIGNED

echo ""
echo "============================================"
echo "SUCCESS! APK: $APK_SIGNED"
ls -lh $APK_SIGNED
echo "============================================"
