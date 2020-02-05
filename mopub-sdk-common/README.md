mopub-sdk-common
================

This repo contains shared assets. For example, MRAID assets live here.

The mopub-android and mopub-ios repos include mopub-sdk-common as a git submodule. This means that to update code in this repo you should do the following:

1. Clone `mopub-sdk-common`
2. Make the desired changes in that repo and follow the regular test/review/submit process
3. Go into the `mopub-sdk-common` directory within `mopub-android` or `mopub-ios` and do `git pull`
4. Go back into the root of `mopub-android` or `mopub-ios` and commit/push the submodule sha change

Grunt
===============

We are using Grunt to minify and remove comments. After making changes, please run

`grunt uglify`

now include the file from `dist/mraid.min.js`.

To get started with grunt, do the following

This updates node to the latest version, seems to be necessary for grunt
`nvm install 0.10`

Installs required packages, like grunt and uglify
`npm install`

Installs the command line tools for grunt
`npm install -g grunt-cli` 
