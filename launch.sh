#!/bin/sh

curl -L -O https://github.com/3arthqu4ke/headlessmc/releases/download/2.3.0/headlessmc-launcher-wrapper-2.3.0.jar

java -jar headlessmc-launcher-wrapper-2.3.0.jar << EOF
download $1
y

fabric $1

login

launch $1 -lwjgl
EOF
