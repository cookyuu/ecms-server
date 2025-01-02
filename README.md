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
<!--![ECMS_SERVER_INFRA](https://github.com/user-attachments/assets/0eeedfda-f0e3-4cc0-8b8e-cd7cb16231f8)-->

## 2. ERD
<img src="https://github.com/user-attachments/assets/b240bd35-76d5-4f7e-836e-50da000c75ff" width="900" height="500"/>
<!--![ERD](https://github.com/user-attachments/assets/b240bd35-76d5-4f7e-836e-50da000c75ff)-->

## 3. 고려해본 문제점

### 3.1 선착순 쿠폰 발급 시 동시성 문제
> 인메모리를 사용하여 속도가 빠르고 단일 thread를 사용하는 Redis의 분산락을 활용하여 선착순 쿠폰 발급 상황에서 다량의 요청이 발생할 시 쿠폰의 재고 관리와 발급 절차를 경험
> 동작 순서
   > 쿠폰 발급 요청 -> Redis(분산락 적용) 재고 카운트 -1 -> 쿠폰 발급 조건 검증 -> 쿠폰 발급 작업 or 재고 카운트 +1 (rollback) 수행 

### 3.2 대규모 주문 트래픽 시 재고 관리
> DB에 Lock(비관적 락)을 적용해 상품 정보의 일관성을 유지하도록 개발 후 Redis 분산락으로 변경
> 변경 이유 : 단순 주문 기능에서는 DB의 비관적 락을 사용해도 문제 없었지만 대규모 주문 트래픽이라는 상황에서는 DB락으로 인한 데드락의 문제 때문에 Redis 분산락으로 변경
> 동작 순서
   > 주문 요청 -> 재고 카운트 -1 -> 주문 정보 검증 -> 주문 작업 수행 or 재고 카운트 +1(rollback)

### 3.3 상품 조회수 증가 시 DB Connection 줄이기

## 4. 그 외 구현사항

### 4.1 Redis 캐싱 적용으로 조회 성능 개선

### 4.2 CI/CD 파이프라인 구축

### 4.3 Prometheus, Grafana 모니터링 시스템 구축

### 4.4 Slack 알림 서비스 구축
