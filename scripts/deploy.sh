#!/bin/bash

cd /home/ubuntu/app

# aws ecr registry 로그인
aws ecr get-login-password --region ap-northeast-2 | sudo docker login --username AWS --password-stdin 467288836803.dkr.ecr.ap-northeast-2.amazonaws.com

# 사용되지 않는 이미지 삭제
sudo docker image prune -af

# 도커 이미지 다운로드
sudo docker pull 467288836803.dkr.ecr.ap-northeast-2.amazonaws.com/backend:latest

sudo docker-compose stop backend

# 도커 컨테이너 실행
sudo docker-compose build --no-cache backend

sudo docker-compose up -d backend