# Robustness of Proactive Traces Used in Retroactive Feature Tracing: Research Artifacts

The code in this repository is used to perform experiments regarding an extended replication study of [Greiner et al.](https://dl.acm.org/doi/10.1145/3646548.3672593),
investigating effects and robustness of a hybrid approach that combines retroactive with proactive feature traces in order to manage Software Product Lines using the [ECCO](https://github.com/jku-isse/ecco) tool. 

In order to rerun the experiments in a Docker container, create a Docker Image usind the Dockerfile in this repository.  (e.g. run "docker build -t experiment ." in the root directory of this repository)
The container will rerun the experiment and create illustrations of the results.
To persist the results, configure two volumes when running the container:
Result SQLite Database: 
    Container Path: /home/user/java/build/resources/main/database
    Host Path: e.g. .../repetition_results/database
Result Illustrations:
    Container Path: /home/user/results/images
    Host Path: e.g. .../repetition_results/images
In case of unforseen problems and failed experiment runs, just restart the container. The experiment will only run scenarios that are still missing according to the configurations.

To change configurations, adapt the properties file at java/src/main/resources/configuration/experiment.properties, as well as the configurations in python/src/plotting/result_plotting.py.
The experiment uses extraction results from the [VEVOS](https://github.com/VariantSync/VEVOS_Extraction) tool as ground truth. 
VEVOS analysed repositories of the subject systems that only contain a single commit to create this ground truth.
