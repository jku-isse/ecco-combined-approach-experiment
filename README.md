# Robustness of Proactive Traces Used in Retroactive Feature Tracing: Research Artifact


The code in this repository is used to perform experiments regarding an extended replication study of [Greiner et al.](https://dl.acm.org/doi/10.1145/3646548.3672593),
investigating effects and robustness of a hybrid approach that combines retroactive with proactive feature traces in order to manage Software Product Lines using the [ECCO](https://github.com/jku-isse/ecco) tool. 

The experiment uses extraction results from the [VEVOS](https://github.com/VariantSync/VEVOS_Extraction) tool as ground truth. 
VEVOS analysed repositories of the subject systems that only contain a single commit to create this ground truth.


## Contents
This repository contains the setup for running the experiment and plotting the results. Specifically:

- [java](java) contains the gradle-based project which runs the experiment
- [experiment.properties](java/src/main/resources/configuration/experiment.properties) contains the properties with which the experiment is run
- [python](python) contains the scripts to analyze the results and to plot the graphs shown in the paper (and additional ones)
- [results](results) contains all results that we report in the paper and additional ones which are left from the paper for space reasons. Particularly,
    - [images](results/images) contains the plots created for all subject systems and for the results of the replicated study
    - [improvements.json](results/improvements/improvements.json) shows the absolute difference of how the median scores have improved between 0% and 5% added proactive traces. 
 

## Replication

To rerun the experiments, you need create a Docker Image using the Dockerfile in this repository.  
Run in the root directory of this repository:

```shell
docker build -t experiment .
```

The container will rerun the experiment. 
It collects the results of the single runs in a sqllite database and uses that data to plot the results.

### Persisting results 

To persist the results, you need to configure two volumes when running the container:

* Result SQLite Database: 
    - Container Path: /home/user/java/build/resources/main/database
    - Host Path: e.g. .../repetition_results/database
* Result Illustrations:
    - Container Path: /home/user/results/images
    - Host Path: e.g. .../repetition_results/images

### Restarting containers

In case of unforseen problems and failed experiment runs, just restart the container. 
The experiment will only run scenarios that are still missing according to the configurations.

### Changing the experiment's setup

To change the experimental setup configurations, adapt the [properties file](java/src/main/resources/configuration/experiment.properties) and the [plotting configurations](python/src/plotting/result_plotting.py).

