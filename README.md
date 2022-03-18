# Eclipse BIRT [![Build Status](https://travis-ci.com/hvbtup/birt.svg?branch=tup_main)](https://travis-ci.com/hvbtup/birt)
The open source Eclipse BIRT reporting and data visualization project.

**This is not the official Eclipse repository!**

This repository was originally forked from `flugtiger/birt`, because at that time that was the only BIRT repo that seemed to work.
As of today (March 2022), it is more or less up-to-date with the official Eclipse repo `eclipse/BIRT`,
so if you want to compare it at the source level, you should compare
this repo's `tup_main` branch with the official `master` branch.

## Reasons for this fork and differences to the official Eclipse repository

For more information about this repository see our [wiki pages](https://github.com/hvbtup/birt/wiki)

If you are just looking for the official BIRT, don't use this - instead use the [official repo](https://github.com/eclipse/birt/)

## BIRT documentation ##

The new [BIRT website](https://eclipse.org/birt) contains a lot of useful information.
It is in fact a wiki, though not as open as other wikis.
The web site still could use a lot of improvement - your help is welcome.

## Using a pre-built BIRT

If you just want to start creating reports, you can use a pre-built BIRT version.
See the releases page for the all-in-one designer and the POJO runtime.

Binary releases of the official BIRT are available on the Eclipse web site.
An official 4.9.0 release is available since 2022-03-16.
You can find binary artifacts for each PR on https://github.com/eclipse/birt/actions

## Building BIRT

Building BIRT has become relatively easy since 2021/2022. Just [see eclipse/birt](https://github.com/eclipse/birt/).
Wim Jongman also created an excellent video tutorial.

Just remember to replace `eclipse/birt` with `hvbtup/birt` and the `master` branch with `tup_main` when configuring GIT.
