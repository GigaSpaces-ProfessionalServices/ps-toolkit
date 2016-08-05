#!/bin/bash
set -o errexit

java -cp "../zombie-finder.jar:../lib/*:../config" com.zombiefinder.app.ZombieFinder dev
