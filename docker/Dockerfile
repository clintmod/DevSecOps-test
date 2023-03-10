ARG BASE_IMAGE
FROM ${BASE_IMAGE} AS base

# Install dependencies

RUN apt-get update && apt-get install -y \
    python2 \
    python3 \
    python3-pip \
    r-base \
    wget

WORKDIR /app
COPY requirements.txt .
RUN wget https://bootstrap.pypa.io/pip/2.7/get-pip.py
RUN python2 get-pip.py && rm get-pip.py
RUN python2 -m pip install -r requirements.txt
RUN python3 -m pip install -r requirements.txt
RUN which R
EXPOSE 8000/tcp
COPY . .
CMD ["python3", "simple_http.py"] 
