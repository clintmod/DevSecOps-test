export

include vars.mk

docker-build:
	@echo
	@echo "Building $(IMAGE_NAME)"
	@echo
	docker build --pull \
	-f docker/Dockerfile \
	-t $(IMAGE_NAME) \
	--build-arg BASE_IMAGE=$(BASE_IMAGE) \
	.

reports:
	mkdir -p reports

docker-login:
	@echo
	@echo "Logging into docker hub as $(DOCKER_HUB_USER)"
	@echo
	@docker login -u $(DOCKER_HUB_USER) -p $(DOCKER_HUB_PASSWORD)

docker-push: docker-login
	@echo
	@echo "Pushing $(IMAGE_NAME) to docker hub"
	@echo
	docker push $(IMAGE_NAME)

docker-scan: reports
	@echo
	@echo "Scanning docker image $(IMAGE_NAME) for vulnerabilities with Trivy"
	@echo
	scripts/trivy-scan.sh

k3d-install:
	apt install k3d

k3d-cluster-create:
	k3d cluster create -p "8081:80@loadbalancer" --agents 2

kubectl-create-deployment:
	kubectl create deployment nginx --image=nginx

printenv:
	env | sort

test-jenkinsfile:
	docker run --rm -v $(PWD):/home/groovy/app groovy:4.0.0 \
		bash -c "cd /home/groovy/app && \
		groovy -cp scripts/jenkinsfile scripts/jenkinsfile/Tests.groovy"

test-jenkinsfile-local:
	groovy -cp scripts/jenkinsfile scripts/jenkinsfile/Tests.groovy
