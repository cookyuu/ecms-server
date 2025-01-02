# ECMS
> 이커머스 도메인에서 겪을 수 있는 문제점을 고민해보기 위한 프로젝트

## 목차
[1. 인프라 아키텍처](#1.-인프라-아키텍처)
[2. ERD](#2.-ERD)
[3. 고려해본 문제점](#3.-고려해본-문제점)
   [3.1 선착순 쿠폰 발급 시 동시성 문제](#3.1-선착순-쿠폰-발급-시-동시성-문제)
   [3.2 대규모 주문 트래픽 발생시 재고 관리](#3.2-대규모-주문-트래픽-발생-시-재고-관리)
   [3.3 상품 조회수 증가 시 DB Connection 횟수 관리](#3.3-상품-조회수-증가-시-DB-connection-줄이기)
[4. 그 외 구현사항](#4-그-외-구현사항)
   [4.1 Redis 캐싱 적용으로 조회 성능 개선](#4.1-Redis-캐싱-적용으로-조회-성능-개선)
   [4.2 CI/CD 파이프라인 구축](#4.2-CI/CD-파이프라인-구축)
   [4.3 Promethus, Grafana 모니터링 시스템 구축](#4.3-Promethus,-Grafana-모니터링-시스템-구축)
   [4.4 Slack 알림 서비스 구축](#4.4-Slack-알림-서비스-구축)
...

## 1. 인프라 아키텍처

<img width="983" alt="ECMS_SERVER_INFRA" src="https://github.com/user-attachments/assets/0eeedfda-f0e3-4cc0-8b8e-cd7cb16231f8">

## 2. ERD
![953DC97B-B968-484A-BBDF-04FA48B0992B](https://github.com/user-attachments/assets/b240bd35-76d5-4f7e-836e-50da000c75ff)

## 3. 고려해본 문제점
   ### 3.1 선착순 쿠폰 발급 시 동시성 문제
   ### 3.2 대규모 주문 트래픽 발생 시 재고 관리
   ### 3.3 상품 조회수 증가 시 DB connection 줄이기
  
## 4. 그 외 구현사항
   ### 4.1 Redis 캐싱 적용으로 조회 성능 개선
   ### 4.2 CI/CD 파이프라인 구축
   ### 4.3 Promethus, Grafana 모니터링 시스템 구축
   ### 4.4 Slack 알림 서비스 구축
<!--

## 설치 방법

```
docker-compose up --build -d
docker -f compose docker-compose-monitoring.yml up -d
```

## 사용 예제

스크린 샷과 코드 예제를 통해 사용 방법을 자세히 설명합니다.

_더 많은 예제와 사용법은 [Wiki][wiki]를 참고하세요._


## 업데이트 내역

* 1.0.0
  
* 1.0.1
    * 
-->
<!-- Markdown link & img dfn's -->
[npm-image]: https://img.shields.io/npm/v/datadog-metrics.svg?style=flat-square
[npm-url]: https://npmjs.org/package/datadog-metrics
[npm-downloads]: https://img.shields.io/npm/dm/datadog-metrics.svg?style=flat-square
[travis-image]: https://img.shields.io/travis/dbader/node-datadog-metrics/master.svg?style=flat-square
[travis-url]: https://travis-ci.org/dbader/node-datadog-metrics
[wiki]: https://github.com/yourname/yourproject/wiki

