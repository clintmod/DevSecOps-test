# Answers

The following test was created to evaluate DevSecOps skills.

## General questions - Docker, CVEs, CI/CD, monitoring

1. Create an image with python2, python3, R, install a set of requirements and upload it to docker hub.
    
**Answer**:

- You can find the image here: https://hub.docker.com/r/clintmod/devsecops-test/tags
- The git repo is here: https://github.com/clintmod/DevSecOps-test

2. For the previously created image:

**Answer**:

a. Share build times

- Clean build time is: 1m 22s 967ms
- Clean build when adding a pylint to requirements.txt: 16s820ms
- Cached build time when just changing code is: 2s 252ms

b. How would you improve build times?

- Use a faster computer: Faster CPU and faster hard drive (e.g. nvme)
- Use layers with caching that you can pull from an artifact repo

3. Scan the recently created container and evaluate the CVEs that it might contain.

**Answer**:

a. Create a report of your findings and follow best practices to remediate the CVE

- Here is the report (login: guest/SuperSecurePassword1234567*()): https://jenkins.vectorscape.com/blue/organizations/jenkins/devsecops-test/detail/devsecops-test/1/pipeline

b. What would you do to avoid deploying malicious packages?

- I failed the build for the scan
- Here is the commit with the fix: https://github.com/clintmod/DevSecOps-test/commit/1d657bf8ce0913e76eb675f646b613cedc6c93b4
- Here is the build passing: https://jenkins.vectorscape.com/blue/organizations/jenkins/devsecops-test/detail/devsecops-test/2/pipeline

4. Use the created image to create a kubernetes deployment with a command that will keep the pod running

**Answer**:

- Here is the deployment/service: https://github.com/clintmod/DevSecOps-test/tree/main/k8s
- I wrote a python script to start an simple http web server that serves up the contents of the static directory
- The static dir contains an index.html file that has the word `hello` in it 

5. Expose the deployed resource

**Answer**:

- Here is the ingressroute: https://github.com/clintmod/DevSecOps-test/tree/main/k8s
- I used k3d/k3s which uses traefik as an ingress controller by default
- I ran this to make sure the deployment is actually working and exposed on the Jenkins server:

```
kubectl cluster-info
jenkins@localhost:~/workspace/devsecops-test$ kubectl cluster-info
Kubernetes control plane is running at https://0.0.0.0:46515
CoreDNS is running at https://0.0.0.0:46515/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
Metrics-server is running at https://0.0.0.0:46515/api/v1/namespaces/kube-system/services/https:metrics-server:https/proxy

To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.
jenkins@localhost:~/workspace/devsecops-test$ kubectl get pods
NAME                              READY   STATUS    RESTARTS   AGE
devsecops-test-79cf988dd4-4p5w4   1/1     Running   0          28s
jenkins@localhost:~/workspace/devsecops-test$ curl http://localhost:8081
hello
```

6. Every step mentioned above have to be in a code repository with automated CI/CD

**Answer**:

- I found a vps with 4 cores and 8gb on sale for $2/month/6 months at ionos: https://www.ionos.com/servers/vps#plans
- Installed java, jenkins, nginx (proxy) and certbot (automated free ssl)
- I setup a job with this Jenkinsfile: https://github.com/clintmod/DevSecOps-test/blob/main/Jenkinsfile
- I wrote snapshot based unit tests for the Jenkinsfile here (my own design): https://github.com/clintmod/DevSecOps-test/tree/main/scripts/jenkinsfile
- It currently only watches the main branch (but it's easy to watch all branches or even watch the entire github account)


7. How would you monitor the above deployment? Explain or implement the tools that you would use

**Answer**:

- I would use Prometheus, Grafana, Alertmanager to just generically monitor all things in the cluster and send notifications to slack like I did at Flowcast.
- I could also use k8s dashboard to manually (yuk) watch it succeed
- I could also use kubectl in watch mode to watch the deployment go green
- I would also setup the correct probes so that I know if the containers are healthy
- I would run curl to check the value of the result is `hello`


## Project

Using Kubernetes you need to provide all your employees with a way of launching multiple development environments (different base images, requirements, credentials, others). 

The following are the basic needs for it:

1. UI, CI/CD, workflow or other tool that will allow people to select options for:

    a. Base image
    b. Packages
    c. Mem/CPU/GPU requests

**Answer**:

- Portainer:
    - I would use portainer for this: https://www.portainer.io/features
    - It can manage multiple clusters
    - Has RBAC built in and can manage RBAC in clusters
    - Free for 5 nodes (still worth paying for)
    - Also as a free CE edition: https://www.portainer.io/blog/portainer-community-edition-ce-vs-portainer-business-edition-be-whats-the-difference
    - It has some gitops features as well: https://www.portainer.io/gitops-automation
    - Here's how I would install it: https://docs.portainer.io/start/install/server/kubernetes/baremetal#deploy-using-helm
```bash
helm repo add portainer https://portainer.github.io/k8s/
helm repo update
helm upgrade --install --create-namespace -n portainer portainer portainer/portainer --set enterpriseEdition.enabled=true --set tls.force=true
```

- (Alternative) Jenkins:
    - Being able to select Base Image, Packages, Mem/CPU,GPU requests can be accomplished with Jenkins parameters: https://www.jenkins.io/doc/book/pipeline/syntax/#parameters **and** using Helm templating
    - I could add this to the existing Jenkinsfile in the project above
    - This is significantly more work than installing portainer and does not have as many features but would have the benefit of giving you more control over what's running the cluster


- (Alternative) Command Line Utility:
    - You could also just distribute a command line utility that satisfies this requirement

#### -----------------------

**Side Note**:

- This solution above likely wouldn't be as simple as specifying python libs in the `requirements.txt` file for an image that requires the installation of data science libraries that require native c++ dependencies.
- The installation of the python dependencies usually requires the installation of additional software (e.g. xgboost, TensorFlow, keras, scikit, nvidia drivers for gpu access) via a package manager (yum, apt) or be compiled with gcc.
- These python libs and their requirements vary wildly depending on the type of data that you're analyzing (columnar, images, video, text) 

#### -----------------------

2. Monitor each environment and make sure that:

    a. Resources request is accurate (requested vs used)

**Answer**:

- I would use KubeCost: https://www.kubecost.com/pricing
- I would also set resource quotas on namespaces to control cost: https://kubernetes.io/docs/concepts/policy/resource-quotas/

