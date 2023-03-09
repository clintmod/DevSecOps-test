# Assignment

The following test was created to evaluate DevSecOps skills.

## General questions - Docker, CVEs, CI/CD, monitoring

1. Create an image with python2, python3, R, install a set of requirements and upload it to docker hub.
2. For the previously created image
a. Share build times
b. How would you improve build times?
3. Scan the recently created container and evaluate the CVEs that it might contain.
a. Create a report of your findings and follow best practices to remediate the CVE
b. What would you do to avoid deploying malicious packages?
4. Use the created image to create a kubernetes deployment with a command that will keep the pod running
5. Expose the deployed resource
6. Every step mentioned above have to be in a code repository with automated CI/CD
7. How would you monitor the above deployment? Explain or implement the tools that you would use

## Project

Using kubernetes you need to provide all your employees with a way of launching multiple development environments (different base images, requirements, credentials, others). 

The following are the basic needs for it:

1. UI, CI/CD, workflow or other tool that will allow people to select options for:
a. Base image
b. Packages
c. Mem/CPU/GPU requests
2. Monitor each environment and make sure that:
a. Resources request is accurate (requested vs used)
