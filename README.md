# ECMS

> 이커머스 도메인에서 겪을 수 있는 문제점을 고민해보기 위한 프로젝트

## 목차

1. [인프라 아키텍처](#1-인프라-아키텍처)
2. [ERD](#2-erd)
3. [고려해본 문제점](#3-고려해본-문제점)
   1. [선착순 쿠폰 발급 시 동시성 문제](#31-선착순-쿠폰-발급-시-동시성-문제)
   2. [대규모 주문 트래픽 발생 시 재고 관리](#32-대규모-주문-트래픽-발생-시-재고-관리)
   3. [상품 조회수 증가 시 DB Connection 줄이기](#33-상품-조회수-증가-시-db-connection-줄이기)
4. [그 외 구현사항](#4-그-외-구현사항)
   1. [Redis 캐싱 적용으로 조회 성능 개선](#41-redis-캐싱-적용으로-조회-성능-개선)
   2. [CI/CD 파이프라인 구축](#42-cicd-파이프라인-구축)
   3. [Prometheus, Grafana 모니터링 시스템 구축](#43-prometheus-grafana-모니터링-시스템-구축)
   4. [Slack 알림 서비스 구축](#44-slack-알림-서비스-구축)

## 1. 인프라 아키텍처
<img src="https://github.com/user-attachments/assets/0eeedfda-f0e3-4cc0-8b8e-cd7cb16231f8" width="200" height="400"/>
<!--![ECMS_SERVER_INFRA](https://github.com/user-attachments/assets/0eeedfda-f0e3-4cc0-8b8e-cd7cb16231f8)-->

## 2. ERD
<img src="https://github.com/user-attachments/assets/b240bd35-76d5-4f7e-836e-50da000c75ff" width="200" height="400"/>
<!--![ERD](https://github.com/user-attachments/assets/b240bd35-76d5-4f7e-836e-50da000c75ff)-->

## 3. 고려해본 문제점

### 3.1 선착순 쿠폰 발급 시 동시성 문제

### 3.2 대규모 주문 트래픽 발생 시 재고 관리

### 3.3 상품 조회수 증가 시 DB Connection 줄이기

## 4. 그 외 구현사항

### 4.1 Redis 캐싱 적용으로 조회 성능 개선

### 4.2 CI/CD 파이프라인 구축

### 4.3 Prometheus, Grafana 모니터링 시스템 구축

### 4.4 Slack 알림 서비스 구축
