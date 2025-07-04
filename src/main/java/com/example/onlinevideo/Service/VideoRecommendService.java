package com.example.onlinevideo.Service;

import com.example.onlinevideo.Entity.Video;
import com.example.onlinevideo.Mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VideoRecommendService {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String RECOMMEND_VIDEO_IDS_KEY = "video:recommend:ids";
    private static final String USED_VIDEO_IDS_KEY_PREFIX = "video:used:";
    private static final int RECOMMEND_POOL_SIZE = 40; // 最新20条+最热20条

    // 获取推荐视频
    public List<Video> getRecommendVideos(int page, int size, String token) {
        // 1. 检查推荐池是否存在，不存在则初始化
        if (!redisTemplate.hasKey(RECOMMEND_VIDEO_IDS_KEY)) {
            initRecommendPool();
        }

        // 2. 获取已使用的视频ID集合
        Set<Object> usedVideoIds = redisTemplate.opsForSet().members(USED_VIDEO_IDS_KEY_PREFIX + token);
        if (usedVideoIds == null) {
            usedVideoIds = Collections.emptySet();
        }

        // 3. 从推荐池中获取未使用的视频ID
        Set<Integer> availableVideoIds = new LinkedHashSet<>();
        List<Integer> allRecommendIds = (List<Integer>) redisTemplate.opsForValue().get(RECOMMEND_VIDEO_IDS_KEY);

        for (Integer videoId : allRecommendIds) {
            if (!usedVideoIds.contains(videoId)) {
                availableVideoIds.add(videoId);
                if (availableVideoIds.size() >= size) {
                    break;
                }
            }
        }

        // 4. 如果推荐池中还有未使用的视频ID，则获取视频详情
        List<Video> result = new ArrayList<>();
        if (!availableVideoIds.isEmpty()) {
            // 打乱顺序
            List<Integer> shuffledIds = new ArrayList<>(availableVideoIds);
            Collections.shuffle(shuffledIds);

            // 获取视频详情
            result = videoMapper.selectVideosByIds(shuffledIds.subList(0, Math.min(size, shuffledIds.size())));

            // 记录已使用的视频ID
            if (!result.isEmpty()) {
//                Set<Integer> usedIds = result.stream().map(Video::getVideoId).collect(Collectors.toSet());
                usedVideoIds = result.stream().map(Video::getVideoId).collect(Collectors.toSet());
                redisTemplate.opsForSet().add(USED_VIDEO_IDS_KEY_PREFIX + token, usedVideoIds.toArray());
                // 设置过期时间为1小时
                redisTemplate.expire(USED_VIDEO_IDS_KEY_PREFIX + token, 1, TimeUnit.HOURS);
            }

            // 如果已经满足请求数量，直接返回
            if (result.size() >= size) {
                // 随机打乱视频列表
                Collections.shuffle(result);
                return result;
            }
        }

        /*// 5. 如果推荐池不足，则从数据库按video_id逆序获取剩余视频
        int remainingSize = size - result.size();
        if (remainingSize > 0) {
            List<Video> remainingVideos = videoMapper.selectVideosOrderByIdDesc(
                    page * size, remainingSize);
            result.addAll(remainingVideos);
        }*/
        // 5. 如果推荐池不足，则从数据库按video_id逆序获取剩余视频
        int remainingSize = size - result.size();
        if (remainingSize > 0) {
            // 获取已使用的视频ID集合
            List<Integer> usedIds = usedVideoIds.stream().map(Object::toString).map(Integer::parseInt).collect(Collectors.toList());
            List<Video> remainingVideos = videoMapper.selectVideosOrderByIdDesc(
                    page * size, remainingSize, usedIds);
            result.addAll(remainingVideos);

            // 记录已使用的视频ID
            if (!result.isEmpty()) {
                Set<Integer> usedIds1 = result.stream().map(Video::getVideoId).collect(Collectors.toSet());
                redisTemplate.opsForSet().add(USED_VIDEO_IDS_KEY_PREFIX + token, usedIds1.toArray());
                // 设置过期时间为1小时
                redisTemplate.expire(USED_VIDEO_IDS_KEY_PREFIX + token, 1, TimeUnit.HOURS);
            }
        }

        // 随机打乱视频列表
        Collections.shuffle(result);
        return result;
    }

    // 初始化推荐池
    private void initRecommendPool() {
        // 获取最新20条视频ID
        List<Integer> newestVideoIds = videoMapper.selectNewestVideoIds(20);
        // 获取最热40条视频ID
        List<Integer> hottestVideoIds = videoMapper.selectHottestVideoIds(40);

        // 合并并去重
        Set<Integer> recommendIdPool = new LinkedHashSet<>();
        recommendIdPool.addAll(newestVideoIds);
        recommendIdPool.addAll(hottestVideoIds);

        // 存入Redis，有效期1小时
        redisTemplate.opsForValue().set(
                RECOMMEND_VIDEO_IDS_KEY,
                new ArrayList<>(recommendIdPool),
                1,
                TimeUnit.HOURS
        );
    }

    // 重置推荐池（前端刷新时调用）
    public void resetRecommendPool(String token) {
        // 删除用户的已使用记录
        redisTemplate.delete(USED_VIDEO_IDS_KEY_PREFIX + token);
        // 重新初始化推荐池
        initRecommendPool();
    }
}










/*
package com.example.onlinevideo.Service;

import com.example.onlinevideo.Entity.Video;
import com.example.onlinevideo.Mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VideoRecommendService {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String RECOMMEND_VIDEOS_KEY = "video:recommend:pool";
    private static final String USED_VIDEOS_KEY_PREFIX = "video:used:";
    private static final int RECOMMEND_POOL_SIZE = 40; // 最新20条+最热20条

    // 获取推荐视频
    public List<Video> getRecommendVideos(int page, int size, String token) {
        // 1. 检查推荐池是否存在，不存在则初始化
        if (!redisTemplate.hasKey(RECOMMEND_VIDEOS_KEY)) {
            initRecommendPool();
        }

        // 2. 获取已使用的视频ID集合
        Set<Object> usedVideoIds = redisTemplate.opsForSet().members(USED_VIDEOS_KEY_PREFIX + token);

        // 3. 从推荐池中获取未使用的视频
        List<Video> result = new ArrayList<>();
        List<Video> recommendPool = (List<Video>) redisTemplate.opsForValue().get(RECOMMEND_VIDEOS_KEY);

        // 过滤掉已使用的视频
        List<Video> availableVideos = recommendPool.stream()
                .filter(v -> usedVideoIds == null || !usedVideoIds.contains(v.getVideoId()))
                .collect(Collectors.toList());

        // 4. 如果推荐池中还有未使用的视频，则随机选取
        if (!availableVideos.isEmpty()) {
            int toIndex = Math.min(size, availableVideos.size());
            Collections.shuffle(availableVideos);
            result = availableVideos.subList(0, toIndex);

            // 记录已使用的视频ID
            if (!result.isEmpty()) {
                Set<Integer> usedIds = result.stream().map(Video::getVideoId).collect(Collectors.toSet());
                redisTemplate.opsForSet().add(USED_VIDEOS_KEY_PREFIX + token, usedIds.toArray());
                // 设置过期时间为1小时
                redisTemplate.expire(USED_VIDEOS_KEY_PREFIX + token, 1, TimeUnit.HOURS);
            }

            // 如果已经满足请求数量，直接返回
            if (result.size() >= size) {
                return result;
            }
        }

        // 5. 如果推荐池不足，则从数据库按video_id逆序获取剩余视频
        int remainingSize = size - result.size();
        if (remainingSize > 0) {
            List<Video> remainingVideos = videoMapper.selectVideosOrderByIdDesc(
                    page * size, remainingSize);
            result.addAll(remainingVideos);
        }

        return result;
    }

    // 初始化推荐池
    private void initRecommendPool() {
        // 获取最新10条视频
        List<Video> newestVideos = videoMapper.selectNewestVideos(20);
        // 获取最热10条视频
        List<Video> hottestVideos = videoMapper.selectHottestVideos(20);

        // 合并并去重
        Set<Video> recommendPool = new LinkedHashSet<>();
        recommendPool.addAll(newestVideos);
        recommendPool.addAll(hottestVideos);

        // 存入Redis，有效期1小时
        redisTemplate.opsForValue().set(RECOMMEND_VIDEOS_KEY,
                new ArrayList<>(recommendPool), 1, TimeUnit.HOURS);
    }

    // 重置推荐池（前端刷新时调用）
    public void resetRecommendPool(String token) {
        // 删除用户的已使用记录
        redisTemplate.delete(USED_VIDEOS_KEY_PREFIX + token);
        // 重新初始化推荐池
        initRecommendPool();
    }
}*/
