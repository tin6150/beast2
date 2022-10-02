
Containerazation of BEAST 2
===========================

(Original README below this section)

To run BEAST 2 via singularity container in HPC node, perform:

```{bash}
singularity pull --name beast2.6.4-beagle.sif  docker://ghcr.io/tin6150/beast2:dock264-beagle
singularity pull --name beast2.6.4-noOCL.sif   docker://ghcr.io/tin6150/beast2:dock264-noOCL

./beast2.6.4-noOCL.sif -beagle_CPU testHKY.xml

# GPU need additional singularity flag, thus run as:
singularity exec --nv ./beast2.6.4-noOCL.sif /usr/bin/java -Dlauncher.wait.for.exit=true -Xms256m -Xmx8g -Duser.language=en -cp /opt/gitrepo/beast/lib/launcher.jar beast.app.beastapp.BeastLauncher -beagle_info

```


(Original README follows)


BEAST 2
=======

[![Build Status](https://github.com/CompEvol/beast2/workflows/Core%20tests/badge.svg)](https://github.com/CompEvol/beast2/actions?query=workflow%3A%22Core+tests%22)

BEAST is a cross-platform program for Bayesian inference using MCMC of
molecular sequences. It is entirely orientated towards rooted,
time-measured phylogenies inferred using strict or relaxed molecular
clock models. It can be used as a method of reconstructing phylogenies
but is also a framework for testing evolutionary hypotheses without
conditioning on a single tree topology. BEAST uses MCMC to average
over tree space, so that each tree is weighted proportional to its
posterior probability. We include a simple to use user-interface
program for setting up standard analyses and a suit of programs for
analysing the results.

NOTE: This directory contains the BEAST 2 source code, and is
therefore of interest primarily to BEAST 2 developers.  For binary
releases, user tutorials and other information you should visit the
project website at www.beast2.org.

Development Rules and Philosophy
--------------------------------

Aspects relating to BEAST 2 development such as coding style, version
numbering and design philosophy are discussed on the BEAST 2 web page at
http://www.beast2.org/package-development-guide/core-development-rules/.

License
-------

BEAST 2 is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as published
by the Free Software Foundation; either version 2.1 of the License, or
(at your option) any later version. A copy of the license is contained
in the file COPYING, located in the root directory of this repository.

This software is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License contained in the file COPYING for more
details.
