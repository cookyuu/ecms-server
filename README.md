# ECMS

> 이커머스 도메인에서 겪을 수 있는 문제점을 고민해보기 위한 프로젝트

## 목차

1. [인프라 아키텍처](#1-인프라-아키텍처)
2. [ERD](#2-erd)
3. [고려해본 문제점](#3-고려해본-문제점)
   1. [선착순 쿠폰 발급 시 동시성 문제](#31-선착순-쿠폰-발급-시-동시성-문제)
   2. [대규모 주문 트래픽 시 재고 관리](#32-대규모-주문-트래픽-시-재고-관리)
   3. [상품 조회수 증가 시 DB Connection 줄이기](#33-상품-조회수-증가-시-db-connection-줄이기)
4. [그 외 구현사항](#4-그-외-구현사항)
   1. [Redis 캐싱 적용으로 조회 성능 개선](#41-redis-캐싱-적용으로-조회-성능-개선)
   2. [CI/CD 파이프라인 구축](#42-cicd-파이프라인-구축)
   3. [Prometheus, Grafana 모니터링 시스템 구축](#43-prometheus-grafana-모니터링-시스템-구축)
   4. [Slack 알림 서비스 구축](#44-slack-알림-서비스-구축)

## 1. 인프라 아키텍처
<img src="https://github.com/user-attachments/assets/0eeedfda-f0e3-4cc0-8b8e-cd7cb16231f8" width="900" height="500"/>

## 2. ERD
<img src="https://github.com/user-attachments/assets/b240bd35-76d5-4f7e-836e-50da000c75ff" width="900" height="500"/>

## 3. 고려해본 문제점

### 3.1 선착순 쿠폰 발급 시 동시성 문제
> 인메모리를 사용하여 속도가 빠르고 단일 thread를 사용하는 Redis의 분산락을 활용하여 선착순 쿠폰 발급 상황에서 다량의 요청이 발생할 시 쿠폰의 재고 관리와 발급 절차를 경험 </br></br>
> 동작 순서 </br>
> 쿠폰 발급 요청 -> Redis(분산락 적용) 재고 카운트 -1 -> 쿠폰 발급 조건 검증 -> 쿠폰 발급 작업 or 재고 카운트 +1 (rollback)</br>
> <a href="src/main/java/com/cookyuu/ecms_server/domain/coupon/facade/CouponFacade.java">쿠폰 발급 소스코드</a>
> 

### 3.2 대규모 주문 트래픽 시 재고 관리
> DB에 Lock(비관적 락)을 적용해 상품 정보의 일관성을 유지하도록 개발</br></br>

> 동작 순서 </br>
> 주문 요청 -> 재고 카운트 -1 -> 주문 정보 검증 -> 주문 작업 수행 or 재고 카운트 +1(rollback)</br>
> <a href="https://github.com/cookyuu/ecms-server/blob/59d3302651469c5c7e053354baa1c21043d50f4d/src/main/java/com/cookyuu/ecms_server/domain/order/service/OrderService.java">상품 주문 소스코드</a>

### 3.3 상품 조회수 증가 시 DB Connection 줄이기
> 상품 조회시 매 요청마다 DB에 조회수를 업데이트 하는 대신 Redis에 조회수를 증가 시키고 스케쥴링을 통해 지정한 시간마다 조회수를 DB에 업데이트하는 방식으로 DB Connection 수를 줄임 </br></br>
> 동작 순서 </br>
> 상품 상세 조회 요청 -> 유저의 방문 여부 검증(쿠키) -> Redis 카운트 -> 지정 시간마다 DB 조회수 업데이트 스케쥴링 </br>
> <a href="https://github.com/cookyuu/ecms-server/blob/59d3302651469c5c7e053354baa1c21043d50f4d/src/main/java/com/cookyuu/ecms_server/domain/product/service/ProductService.java">조회수 증가 소스코드</a> </br>
> <a href="https://github.com/cookyuu/ecms-server/blob/59d3302651469c5c7e053354baa1c21043d50f4d/src/main/java/com/cookyuu/ecms_server/domain/product/executor/ApplyProductHitsExecutor.java">스케쥴링 소스코드</a>

## 4. 그 외 구현사항

### 4.1 Redis 캐싱 적용으로 조회 성능 개선
> 상품, 주문 상세 조회 시 Redis 캐싱으로 DB 쿼리 수를 줄임, Ngrinder 테스트를 통해 Tps 증가 확인 </br>
> 상품 상세 : 399tps -> 478tps 약 20% 증가 (1분, 가상 유저 1000명 기준) </br>
> 주문 상세 : 414tps -> 506tps 약 22% 증가 (1분, 가상 유저 1000명 기준) </br>

</br>
<img src="https://github.com/user-attachments/assets/db30632d-a613-4e0c-ae98-24209af6c76f" width="400" height="400"/>
<img src="https://github.com/user-attachments/assets/539cb970-1bf7-4c00-825d-5ca1ba929281" width="400" height="400"/>
</br>
<img src="https://github.com/user-attachments/assets/526cf2a7-5943-4083-9fa7-9ebc0012e876" width="400" height="400"/>
<img src="https://github.com/user-attachments/assets/38a7b53a-6498-46fd-943f-fe5dc07d6ea9" width="400" height="400"/>

### 4.2 CI/CD 파이프라인 구축
> AWS와 Jenkins를 활용한 CI/CD 파이프라인 구축 </br></br>
> 동작 순서 </br>
> github(main branch) pull request -> Jenkins(EC2) -> ECR -> CodeDeploy -> ECS(fargate)

### 4.3 Prometheus, Grafana 모니터링 시스템 구축
> Prometheus를 통해 Memory, CPU, 트래픽 등 Metric을 수집하여 Grafana를 통해 시각화 하여 모니터링 시스템 구축

### 4.4 Slack 알림 서비스 구축
> Exception Hanlder를 통해 지정 해놓은 예외가 아닌 공통 Exception이 발생하면 Slack 메시지가 발송되는 서비스 구축 </br>
> <a href="https://github.com/cookyuu/ecms-server/blob/59d3302651469c5c7e053354baa1c21043d50f4d/src/main/java/com/cookyuu/ecms_server/global/exception/GlobalExceptionHandler.java"> Exception Handler 소스코드 </a>
> <a href="https://github.com/cookyuu/ecms-server/blob/59d3302651469c5c7e053354baa1c21043d50f4d/src/main/java/com/cookyuu/ecms_server/domain/alert/service/SlackAlertService.java"> Slack 알림 소스코드</a>
> <img width="900" height="500" src="https://github.com/user-attachments/assets/2b1c8ff3-f371-40c1-bee4-e002166f3f6d" />

