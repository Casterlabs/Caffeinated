#!/bin/bash

set -e -o pipefail

APP_ID="co.casterlabs.caffeinated"
APP_NAME="Casterlabs-Caffeinated"
MAIN_CLASS="co.casterlabs.caffeinated.bootstrap.Bootstrap"
VM_OPTIONS="-Xms1M -XX:+UseCompressedOops -XX:+UseSerialGC -XX:MaxHeapFreeRatio=1 -XX:MinHeapFreeRatio=1"
SAUCER4J_VERSION="1.0.0-pre1"

if [[ $@ == *"compile"* ]]; then
    echo "------------ Compiling app ------------"

    cd app
    bash ./mvnw clean package
    cd ..

    echo "------------ Finishing compiling app ---------"
fi

if [[ $@ == *"dist-windows"* ]]; then
    echo "------------ Bundling for Windows ------------"

    java -jar bundler.jar bundle \
        --arch x86_64 --os windows \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 --arg="$VM_OPTIONS" --main $MAIN_CLASS \
        --file app/core/WMC-JsonConsoleWrapper.exe --dependency "app/core/target/Caffeinated.jar" \
        --dependency "https://jitpack.io|com.github.saucer.saucer4j:webview2:$SAUCER4J_VERSION:.jar"

    echo "------------ Finished bundling for Windows ------------"
fi

if [[ $@ == *"dist-macos"* ]]; then
    echo "------------ Bundling for macOS ------------"

    java -jar bundler.jar bundle \
        --arch aarch64 --os macos \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 --arg="-XstartOnFirstThread $VM_OPTIONS" --main $MAIN_CLASS \
        --dependency "app/core/target/Caffeinated.jar" \
        --dependency "https://jitpack.io|com.github.saucer.saucer4j:webkit:$SAUCER4J_VERSION:.jar"

    java -jar bundler.jar bundle \
        --arch x86_64 --os macos \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 --arg="-XstartOnFirstThread $VM_OPTIONS" --main $MAIN_CLASS \
        --dependency "app/core/target/Caffeinated.jar" \
        --dependency "https://jitpack.io|com.github.saucer.saucer4j:webkit:$SAUCER4J_VERSION:.jar"

    echo "------------ Finished bundling for macOS ------------"
fi

if [[ $@ == *"dist-linux"* ]]; then
    echo "------------ Bundling for Linux ------------"

    java -jar bundler.jar bundle \
        --arch aarch64 --os gnulinux \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 --arg="$VM_OPTIONS" --main $MAIN_CLASS \
        --dependency "app/core/target/Caffeinated.jar" \
        --dependency "https://jitpack.io|com.github.saucer.saucer4j:webkitgtk:$SAUCER4J_VERSION:.jar"

    java -jar bundler.jar bundle \
        --arch arm --os gnulinux \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 --arg="$VM_OPTIONS" --main $MAIN_CLASS \
        --dependency "app/core/target/Caffeinated.jar" \
        --dependency "https://jitpack.io|com.github.saucer.saucer4j:webkitgtk:$SAUCER4J_VERSION:.jar"

    java -jar bundler.jar bundle \
        --arch x86_64 --os gnulinux \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 --arg="$VM_OPTIONS" --main $MAIN_CLASS \
        --dependency "app/core/target/Caffeinated.jar" \
        --dependency "https://jitpack.io|com.github.saucer.saucer4j:webkitgtk:$SAUCER4J_VERSION:.jar"

    echo "------------ Finished bundling for Linux ------------"
fi

