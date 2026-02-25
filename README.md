# Robustness of Proactive Traces Used in Retroactive Feature Tracing: Research Artifact


This repository provides the facilities to conduct an extended replication study of [Greiner et al.](https://dl.acm.org/doi/10.1145/3646548.3672593). The authors examined the effects on accuracy of proactive traces in a comparison-based feature location technique.

Our work, implemented in this repository, investigates the effects and robustness of exploiting proactive feature traces in retroactive feature tracing to manage Software Product Lines using the variation control system [ECCO](https://github.com/jku-isse/ecco). 
We integrate and reimplement the boosting algorithm in ECCO. Then, we conduct an experiment where we gradually add randomly distributed _5%_ of proactive traces (given by our ground truth, see below) to a given number of variants. 
We examine the resulting trace quality in form of _precision_, _recall_, and _F1 scores_ and whether the improvements are _statistical significant_. 

As extension to the replicated study, we examine the effect of errors in the provided proactive traces. We simulate different developer mistakes that could occur in proactive traces and examine the effect of _25%_, _50%_ and _75%_ of proactive traces containing errors.

The experiment uses extraction results from the [VEVOS](https://github.com/VariantSync/VEVOS_Extraction) tool as ground truth. 
VEVOS analyses the last commit of the subject systems to create the ground truth which holds a presence condition for each line of code in the examined respositories.


## Contents
This repository contains the setup for running the experiment and plotting the results. Specifically:

- [java](java) contains the gradle-based project which condutcts the experiment
- [experiment.properties](java/src/main/resources/configuration/experiment.properties) contains the properties with which the experiment is run
- [python](python) contains the scripts to analyze the results and to plot the graphs shown in the paper (and additional ones)
- [results](results) contains all results that we report in the paper and additional ones which are left from the paper for space reasons. Particularly,
    - [databases](results/databases) contains SQLite databases containing the results of the experiment. Each database contains results regarding one examined subject system.
    - [images](results/images) contains the plots created for all subject systems and for the results of the replicated study
    - [improvements.json](results/improvements/improvements.json) shows the absolute difference of how the median scores have improved between 0% and 5% added proactive traces. 
    - [significance](results/significance) contains results of performed significance tests.

## Replication

To rerun the experiments, you need create a Docker Image using the Dockerfile in this repository.  
Run in the root directory of this repository:

```shell
docker build -t experiment .
```

### Persisting results 

A container that is based on the resulting docker image will rerun the expirements. 
During this rerun, the container collects all results in a SQLlite database and uses that data to plot resulting images.
Without specifying docker volumes, the database and the resulting images will be deleted once the container is destroyed. 
To persist the database and resulting images, you need to configure two docker volumes when running the container:

* Result SQLite Database: 
    - Container Path: /home/user/java/build/resources/main/database
    - Host Path: e.g. C:\experiment_rerun\results\database
* Result Illustrations:
    - Container Path: /home/user/results/images
    - Host Path: e.g. C:\experiment_rerun\results\images

The container paths must not be changed.
The database and the resulting images will be saved at the specified host paths on the host machine.
Configure the host paths to point to existing locations on your machine.

To run the docker container with docker volumes configured, e.g. run the following:

```shell
docker run \
  -v ./repetition_results/database:/home/user/java/build/resources/main/database \
  -v ./repetition_results/images:/home/user/results/images \
  experiment
```

### Restarting containers

In case of unforseen problems and failed experiment runs, just restart the container. 
The experiment will only run scenarios that are still missing according to the configurations.

### Changing the experiment's setup

To change the experimental setup configurations, adapt the [properties file](java/src/main/resources/configuration/experiment.properties).
Creating the resulting images will fail for a changed configuration, as the corresponding plots are tailored to the configurations used in the original experiment. Nevertheless, the container will create a database with resulting data for the changed configuration.
