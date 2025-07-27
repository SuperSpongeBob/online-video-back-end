/*
 Navicat Premium Data Transfer

 Source Server         : zyx
 Source Server Type    : MySQL
 Source Server Version : 80039
 Source Host           : localhost:3306
 Source Schema         : online_video

 Target Server Type    : MySQL
 Target Server Version : 80039
 File Encoding         : 65001

 Date: 27/07/2025 17:46:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `video_comment_id` int NOT NULL AUTO_INCREMENT COMMENT '评论id',
  `video_comment_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评论内容',
  `video_comment_time` bigint NULL DEFAULT 0 COMMENT '评论时间',
  `user_id` int NOT NULL COMMENT '用户id',
  `video_id` int NOT NULL COMMENT '视频id',
  `parent_id` int NULL DEFAULT NULL COMMENT '父评论ID，顶级评论为NULL',
  `reply_to_id` int NULL DEFAULT NULL COMMENT '回复目标用户ID',
  PRIMARY KEY (`video_comment_id`) USING BTREE,
  INDEX `comment_userId_ibfk1`(`user_id` ASC) USING BTREE,
  INDEX `comment_videoId_ibfk2`(`video_id` ASC) USING BTREE,
  CONSTRAINT `comment_userId_ibfk1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `comment_videoId_ibfk2` FOREIGN KEY (`video_id`) REFERENCES `video` (`video_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------

-- ----------------------------
-- Table structure for danmaku
-- ----------------------------
DROP TABLE IF EXISTS `danmaku`;
CREATE TABLE `danmaku`  (
  `danmaku_id` int NOT NULL AUTO_INCREMENT,
  `video_id` int NOT NULL,
  `text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `time` float NULL DEFAULT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`danmaku_id`) USING BTREE,
  INDEX `danmaku_videoId_ibfk1`(`video_id` ASC) USING BTREE,
  INDEX `danmaku_userId_ibfk2`(`user_id` ASC) USING BTREE,
  CONSTRAINT `danmaku_userId_ibfk2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `danmaku_videoId_ibfk1` FOREIGN KEY (`video_id`) REFERENCES `video` (`video_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of danmaku
-- ----------------------------

-- ----------------------------
-- Table structure for history
-- ----------------------------
DROP TABLE IF EXISTS `history`;
CREATE TABLE `history`  (
  `history_id` int NOT NULL AUTO_INCREMENT,
  `video_id` int NOT NULL COMMENT 'video_id和user_id组合成唯一约束键',
  `user_id` int NOT NULL,
  `watched_seconds` int NULL DEFAULT 0,
  `timestamp` bigint NULL DEFAULT NULL COMMENT '观看时的时间戳',
  PRIMARY KEY (`history_id`) USING BTREE,
  UNIQUE INDEX `video_id`(`video_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `history_userId_ibfk2`(`user_id` ASC) USING BTREE,
  CONSTRAINT `history_userId_ibfk2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `history_videoId_ibfk1` FOREIGN KEY (`video_id`) REFERENCES `video` (`video_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of history
-- ----------------------------

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `role_id` int NOT NULL AUTO_INCREMENT,
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, 'ROLE_USER');
INSERT INTO `role` VALUES (2, 'ROLE_VIP');
INSERT INTO `role` VALUES (3, 'ROLE_ADMIN');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `user_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '男或女',
  `user_phone` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录账号',
  `user_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_add_time` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '$2a$10$FPFbAOX5IT/RxnlO.ni6yucwlqsxqtUAapxZPBbOVvKswJVVUWghC', '管理员', '男', '17806573192', '17806573192@163.com', 1745157351176);
INSERT INTO `user` VALUES (2, '$2a$10$r2DpA1z7nNuZripr1Krcy.9/kth/0iaX7oVD97KQrapLHAlvz1yBi', 'VIP用户', '男', '18613163192', '3024799675@qq.com', 1745157718074);
INSERT INTO `user` VALUES (3, '$2a$10$YZkx6Xru92uT8LBaP2qrqOiyQADkc7WmEwRjLYkAK6DSY3zT.a7c6', '普通用户', '男', '17806570000', 'zhang3192@qq.com', 1745157790105);

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `user_role_id` int NOT NULL AUTO_INCREMENT COMMENT '用户角色表id',
  `user_id` int NOT NULL COMMENT '关联用户表id',
  `role_id` int NOT NULL COMMENT '关联角色表id',
  PRIMARY KEY (`user_role_id`) USING BTREE,
  INDEX `user_role_ibfk1`(`user_id` ASC) USING BTREE,
  INDEX `user_role_ibfk2`(`role_id` ASC) USING BTREE,
  CONSTRAINT `user_role_ibfk1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `user_role_ibfk2` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (1, 1, 3);
INSERT INTO `user_role` VALUES (2, 2, 2);
INSERT INTO `user_role` VALUES (3, 3, 1);

-- ----------------------------
-- Table structure for video
-- ----------------------------
DROP TABLE IF EXISTS `video`;
CREATE TABLE `video`  (
  `video_id` int NOT NULL AUTO_INCREMENT COMMENT '视频id',
  `video_album_id` int NULL DEFAULT NULL COMMENT '关联专辑id、多对一',
  `video_is_vip` int NULL DEFAULT 0 COMMENT '是否为VIP视频：0免费、1VIP',
  `video_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频名称',
  `view_count` int NULL DEFAULT 0 COMMENT '视频的播放量',
  `video_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频标题',
  `video_approval_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核状态：未审核(unaudited) | 审核通过(pass) | 禁播(ban)',
  `duration` int NULL DEFAULT 0 COMMENT '视频时长',
  `video_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频路径、相对路径(在服务器中的文件名）',
  `thumbnail_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频缩略图路径',
  PRIMARY KEY (`video_id`) USING BTREE,
  INDEX `video_ibfk1`(`video_album_id` ASC) USING BTREE,
  CONSTRAINT `video_ibfk1` FOREIGN KEY (`video_album_id`) REFERENCES `video_album` (`video_album_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video
-- ----------------------------

-- ----------------------------
-- Table structure for video_album
-- ----------------------------
DROP TABLE IF EXISTS `video_album`;
CREATE TABLE `video_album`  (
  `video_album_id` int NOT NULL AUTO_INCREMENT COMMENT '视频专辑id',
  `user_id` int NULL DEFAULT NULL COMMENT '上传用户id',
  `video_album_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频专辑名',
  `video_post_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '海报路径',
  `video_release_date` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上映日期',
  `video_summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简介',
  `video_channel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '频道：动漫、电影、电视剧等',
  `video_director` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '导演',
  `video_area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地区：中国、日本、欧美、韩国等',
  `video_favorite_number` int NULL DEFAULT 0 COMMENT '收藏数量',
  `video_update_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新时间',
  `video_last_update` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最新更新集数',
  `video_actor` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '演员',
  PRIMARY KEY (`video_album_id`) USING BTREE,
  INDEX `video_album_ibfk1`(`user_id` ASC) USING BTREE,
  CONSTRAINT `video_album_ibfk1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video_album
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
