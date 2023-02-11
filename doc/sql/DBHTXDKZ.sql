/*==============================================================*/
/* DBMS name:      ORACLE Version 11g                           */
/* Created on:     2023/2/9 18:54:36                            */
/*==============================================================*/


drop table TAB_DMS_COMPANY_DCT cascade constraints;

drop table TAB_DMS_CUT_PEOPLE_DCT cascade constraints;

drop table TAB_DMS_NEED_DETAILED cascade constraints;

drop table TAB_DMS_NEED_SOURCES_DCT cascade constraints;

drop table TAB_DMS_RELATION_CI_DCT cascade constraints;

drop table TAB_DMS_RELATION_SS_DCT cascade constraints;

drop table TAB_DMS_USER_NEED cascade constraints;

/*==============================================================*/
/* Table: TAB_DMS_COMPANY_DCT                                   */
/*==============================================================*/
create table TAB_DMS_COMPANY_DCT 
(
   COMPANY_CODE         VARCHAR2(25)         not null,
   COMPANY_NAME         VARCHAR2(25),
   COMPANY_CLASS        INTEGER,
   CREATE_PEOPLE        VARCHAR2(25),
   CREATE_TIME       DATE,
   UPDATE_PEOPLE        VARCHAR2(25),
   UPDATE_TIME          DATE,
   constraint PK_TAB_DMS_COMPANY_DCT primary key (COMPANY_CODE)
);

comment on column TAB_DMS_COMPANY_DCT.COMPANY_CODE is
'单位编码';

comment on column TAB_DMS_COMPANY_DCT.COMPANY_NAME is
'单位名称';

comment on column TAB_DMS_COMPANY_DCT.COMPANY_CLASS is
'单位类别(1:提出单位,0:责任单位)';

comment on column TAB_DMS_COMPANY_DCT.CREATE_PEOPLE is
'创建人';

comment on column TAB_DMS_COMPANY_DCT.CREATE_TIME is
'创建时间';

comment on column TAB_DMS_COMPANY_DCT.UPDATE_PEOPLE is
'修改人';

comment on column TAB_DMS_COMPANY_DCT.UPDATE_TIME is
'修改时间';

/*==============================================================*/
/* Table: TAB_DMS_CUT_PEOPLE_DCT                                */
/*==============================================================*/
create table TAB_DMS_CUT_PEOPLE_DCT 
(
   CUT_PEOPLE_CODE      VARCHAR2(25)         not null,
   CUT_PEOPLE_NAME      VARCHAR2(50),
   CREATE_PEOPLE        VARCHAR2(25),
   CREATE_TIME          DATE,
   UPDATE_PEOPLE        VARCHAR2(25),
   UPDATE_TIME          DATE,
   constraint PK_TAB_DMS_CUT_PEOPLE_DCT primary key (CUT_PEOPLE_CODE)
);

comment on column TAB_DMS_CUT_PEOPLE_DCT.CUT_PEOPLE_CODE is
'提出人编码';

comment on column TAB_DMS_CUT_PEOPLE_DCT.CUT_PEOPLE_NAME is
'提出人名称';

comment on column TAB_DMS_CUT_PEOPLE_DCT.CREATE_PEOPLE is
'创建人';

comment on column TAB_DMS_CUT_PEOPLE_DCT.CREATE_TIME is
'创建时间';

comment on column TAB_DMS_CUT_PEOPLE_DCT.UPDATE_PEOPLE is
'修改人';

comment on column TAB_DMS_CUT_PEOPLE_DCT.UPDATE_TIME is
'修改时间';

/*==============================================================*/
/* Table: TAB_DMS_NEED_DETAILED                                 */
/*==============================================================*/
create table TAB_DMS_NEED_DETAILED 
(
   NEED_CODE            VARCHAR2(25),
   NEED_NAME            VARCHAR2(250),
   NEED_NS              INTEGER,
   NEED_DETAILED        VARCHAR2(250),
   PLAN_COMPLETE_DATE   DATE,
   VERSION              VARCHAR2(250),
   ISSUE                VARCHAR2(250),
   STATE                INTEGER,
   CREATE_PEOPLE        VARCHAR2(25),
   CREATE_TIME          DATE,
   UPDATE_PEOPLE        VARCHAR2(25),
   UPDATE_TIME          DATE
);

comment on column TAB_DMS_NEED_DETAILED.NEED_CODE is
'需求编码';

comment on column TAB_DMS_NEED_DETAILED.NEED_NAME is
'需求名称';

comment on column TAB_DMS_NEED_DETAILED.NEED_NS is
'需求序号';

comment on column TAB_DMS_NEED_DETAILED.NEED_DETAILED is
'分解需求描述';

comment on column TAB_DMS_NEED_DETAILED.PLAN_COMPLETE_DATE is
'计划完成时间';

comment on column TAB_DMS_NEED_DETAILED.VERSION is
'需求完成版本号';

comment on column TAB_DMS_NEED_DETAILED.ISSUE is
'需求issue关联';

comment on column TAB_DMS_NEED_DETAILED.STATE is
'需求响应状态(0:未完成，1:已完成，2:部分完成,3:需要沟通/协调)';

comment on column TAB_DMS_NEED_DETAILED.CREATE_PEOPLE is
'创建人';

comment on column TAB_DMS_NEED_DETAILED.CREATE_TIME is
'创建时间';

comment on column TAB_DMS_NEED_DETAILED.UPDATE_PEOPLE is
'修改人';

comment on column TAB_DMS_NEED_DETAILED.UPDATE_TIME is
'最后修改时间';

/*==============================================================*/
/* Table: TAB_DMS_NEED_SOURCES_DCT                              */
/*==============================================================*/
create table TAB_DMS_NEED_SOURCES_DCT 
(
   NEED_CODE            VARCHAR2(25)         not null,
   NEED_NAME            VARCHAR2(250),
   NEED_SETP            VARCHAR2(250),
   CREATE_TIME          DATE,
   CREATE_PEOPLE        VARCHAR2(25),
   UPDATE_PEOPLE        VARCHAR2(25),
   UPDATE_TIME          DATE,
   constraint PK_TAB_DMS_NEED_SOURCES_DCT primary key (NEED_CODE)
);

comment on column TAB_DMS_NEED_SOURCES_DCT.NEED_CODE is
'需求编码';

comment on column TAB_DMS_NEED_SOURCES_DCT.NEED_NAME is
'需求名称';

comment on column TAB_DMS_NEED_SOURCES_DCT.NEED_SETP is
'需求阶段';

comment on column TAB_DMS_NEED_SOURCES_DCT.CREATE_TIME is
'创建时间';

comment on column TAB_DMS_NEED_SOURCES_DCT.CREATE_PEOPLE is
'创建人';

comment on column TAB_DMS_NEED_SOURCES_DCT.UPDATE_PEOPLE is
'修改人';

comment on column TAB_DMS_NEED_SOURCES_DCT.UPDATE_TIME is
'修改时间';

/*==============================================================*/
/* Table: TAB_DMS_RELATION_CI_DCT                               */
/*==============================================================*/
create table TAB_DMS_RELATION_CI_DCT 
(
   CI_CODE              VARCHAR2(25),
   CI_NAME              VARCHAR2(50),
   CREATE_PEOPLE        VARCHAR2(25),
   CREATE_TIME          DATE,
   UPDATE_PEOPLE        VARCHAR2(25),
   UPDATE_TIME          DATE
);

comment on column TAB_DMS_RELATION_CI_DCT.CI_CODE is
'配置项编码';

comment on column TAB_DMS_RELATION_CI_DCT.CI_NAME is
'配置项名称';

comment on column TAB_DMS_RELATION_CI_DCT.CREATE_PEOPLE is
'创建人';

comment on column TAB_DMS_RELATION_CI_DCT.CREATE_TIME is
'创建时间';

comment on column TAB_DMS_RELATION_CI_DCT.UPDATE_PEOPLE is
'修改人';

comment on column TAB_DMS_RELATION_CI_DCT.UPDATE_TIME is
'修改时间';

/*==============================================================*/
/* Table: TAB_DMS_RELATION_SS_DCT                               */
/*==============================================================*/
create table TAB_DMS_RELATION_SS_DCT 
(
   SS_CODE              VARCHAR2(25)         not null,
   SS_NAME              VARCHAR2(50),
   CREATE_PEOPLE        VARCHAR2(25),
   CREATE_TIME          DATE,
   UPDATE_PEOPLE        VARCHAR2(25),
   UPDATE_TIME          DATE,
   constraint PK_TAB_DMS_RELATION_SS_DCT primary key (SS_CODE)
);

comment on column TAB_DMS_RELATION_SS_DCT.SS_CODE is
'子系统编码';

comment on column TAB_DMS_RELATION_SS_DCT.SS_NAME is
'子系统名称';

comment on column TAB_DMS_RELATION_SS_DCT.CREATE_PEOPLE is
'创建人';

comment on column TAB_DMS_RELATION_SS_DCT.CREATE_TIME is
'创建时间';

comment on column TAB_DMS_RELATION_SS_DCT.UPDATE_PEOPLE is
'修改人';

comment on column TAB_DMS_RELATION_SS_DCT.UPDATE_TIME is
'修改时间';

/*==============================================================*/
/* Table: TAB_DMS_USER_NEED                                     */
/*==============================================================*/
create table TAB_DMS_USER_NEED 
(
   NEED_ID              VARCHAR2(25)         not null,
   NEED_NAME            VARCHAR2(250),
   NEED_SETP            VARCHAR2(250),
   CUT_COMPANY_NAME     VARCHAR2(25),
   CUT_PEOPLE_NAME      VARCHAR2(25),
   CUT_OPINION          VARCHAR2(250),
   CUT_TIME             DATE,
   SS_NAME              VARCHAR2(50),
   CI_NAME              VARCHAR2(50),
   NEED_CODE            VARCHAR2(25),
   CUT_CATEGURY         VARCHAR2(50),
   CLCS                 VARCHAR2(250),
   RESPONSIBLE_UNIT     VARCHAR2(25),
   COMPLETE_DATE        DATE,
   CURRENT_STATUS       VARCHAR2(25),
   RESPONSIBLE_PEOPLE   VARCHAR2(25),
   PHONE                NUMBER(11),
   CREATE_PEOPLE        VARCHAR2(25),
   CREATE_TIME       DATE,
   UPDATE_PEOPLE        VARCHAR2(25),
   UPDATE_TIME          DATE,
   constraint PK_TAB_DMS_USER_NEED primary key (NEED_ID)
);

comment on column TAB_DMS_USER_NEED.NEED_ID is
'需求id';

comment on column TAB_DMS_USER_NEED.NEED_NAME is
'需求来源';

comment on column TAB_DMS_USER_NEED.NEED_SETP is
'需求阶段';

comment on column TAB_DMS_USER_NEED.CUT_COMPANY_NAME is
'提出单位';

comment on column TAB_DMS_USER_NEED.CUT_PEOPLE_NAME is
'提出人';

comment on column TAB_DMS_USER_NEED.CUT_OPINION is
'提出意见';

comment on column TAB_DMS_USER_NEED.CUT_TIME is
'提出时间';

comment on column TAB_DMS_USER_NEED.SS_NAME is
'关联子系统';

comment on column TAB_DMS_USER_NEED.CI_NAME is
'关联配置项';

comment on column TAB_DMS_USER_NEED.NEED_CODE is
'需求标识';

comment on column TAB_DMS_USER_NEED.CUT_CATEGURY is
'提出类别';

comment on column TAB_DMS_USER_NEED.CLCS is
'处理措施';

comment on column TAB_DMS_USER_NEED.RESPONSIBLE_UNIT is
'责任单位';

comment on column TAB_DMS_USER_NEED.COMPLETE_DATE is
'完成时间';

comment on column TAB_DMS_USER_NEED.CURRENT_STATUS is
'现阶段状态';

comment on column TAB_DMS_USER_NEED.RESPONSIBLE_PEOPLE is
'责任人';

comment on column TAB_DMS_USER_NEED.PHONE is
'联系方式';

comment on column TAB_DMS_USER_NEED.CREATE_PEOPLE is
'创建人';

comment on column TAB_DMS_USER_NEED.CREATE_TIME is
'创建时间';

comment on column TAB_DMS_USER_NEED.UPDATE_PEOPLE is
'修改人';

comment on column TAB_DMS_USER_NEED.UPDATE_TIME is
'修改时间';

