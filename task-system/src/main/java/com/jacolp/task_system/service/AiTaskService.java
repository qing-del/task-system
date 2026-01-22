package com.jacolp.task_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jacolp.task_system.entity.PlayerStatus;
import com.jacolp.task_system.entity.Task;
import com.jacolp.task_system.mapper.TaskMapper;
import com.jacolp.task_system.service.impl.TaskServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiTaskService {
    static final  int TASK_STATUS_COMPLETE = 1;

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.api-url}")
    private String apiUrl;

    @Value("${ai.deepseek.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson json处理工具

    @Autowired private TaskMapper taskMapper;

    /**
     * 调用 AI 根据玩家状态生成任务
     * @param player 玩家状态
     * @return 生成的任务
     */
    public Task generateTask(PlayerStatus player) {
        // 测试连接是否成功
        System.out.println("正在尝试连接地址: " + apiUrl);
        try {
            Task condition = new Task();
            condition.setUserId(player.getUserId());
            condition.setStatus(TASK_STATUS_COMPLETE);
            // 先获取历史任务列表
            List<Task> historyTasks = taskMapper.selectByTaskCondition(condition);

            String historyTaskStr = historyTasks.stream()
                    .limit(5)
                    .map(t -> String.format("任务标题：%s\n" +
                            "任务描述：%s\n" +
                            "任务奖励经验：%d\n" +
                            "任务奖励类型：%s\n" +
                            "任务奖励属性点：%d\n" +
                            "任务硬币奖励点：%d\n",
                            t.getTitle(), t.getDescription(), t.getExpReward(),
                            (t.getRewardType()  == 0 ? "精神" : "肉体"), t.getReward(), t.getCoinReward()))
                    .collect(Collectors.joining("\n"));

            if (historyTaskStr.isEmpty()) {
                historyTaskStr = "无 / 新玩家";
            }

            // 1. 构造 Prompt (提示词) - 这是核心！
            // 我们要求 AI 必须返回纯 JSON，不要废话
//            String prompt = String.format(
//                    "你是一个游戏系统。玩家当前状态：等级%d，精神%d，肉体%d。请根据他的短板生成一个现实生活中可执行的锻炼任务。\n" +
//                            "要求返回标准JSON格式，不要包含Markdown标记（如 ```json），格式如下：\n" +
//                            "{\"title\": \"任务标题\", \"description\": \"任务描述\", \"expReward\": 经验值(整数), \"rewardType\": 奖励类型(0精神/1肉体), \"reward\": 属性点(整数)}",
//                    player.getLevel(), player.getSpirit(), player.getBody()
//            );
            String prompt = String.format("""
                你是一个游戏化人生系统的 GM。
                
                【玩家状态】
                - 等级: Lv.%d
                - 精神 (Spirit): %d
                - 体魄 (Body): %d
                
                【最近完成的任务】(请避免重复以下内容):
                %s
                
                【指令】
                请根据玩家当前状态和历史，生成一个新的现实生活挑战任务。
                要求：
                1. 任务难度适中，具备可执行性。
                2. 尝试平衡精神和体魄的训练，如果玩家最近一直在锻炼身体，就给他派发一个阅读或冥想的任务，反之亦然。
                3. **必须**严格返回 JSON 格式，不要包含 Markdown 格式代码块（如 ```json）。
                
                JSON 格式示例:
                {
                    "title": "晨跑3公里",
                    "description": "穿上跑鞋，去公园慢跑3公里，感受清晨的空气。",
                    "expReward": 15,
                    "rewardType": "body",
                    "reward": 1,
                    "coinReward": 10
                }
                """,
                    player.getLevel(), player.getSpirit(), player.getBody(), historyTaskStr
            );

            // 2. 构造请求体 (OpenAI 标准格式)
            ObjectNode requestJson = objectMapper.createObjectNode();
            requestJson.put("model", model);
            requestJson.put("stream", false); // 不流式传输，我们要一次性拿结果

            ArrayNode messages = requestJson.putArray("messages");
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            // 强制 AI 使用 JSON 模式 (DeepSeek 支持)
            requestJson.putObject("response_format").put("type", "json_object");

            String requestBody = objectMapper.writeValueAsString(requestJson);

            // 3. 发送 HTTP 请求 (使用原生 HttpClient)
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 4. 解析结果
            if (response.statusCode() == 200) {
                // DeepSeek 返回的结构是 nested 的，我们需要扒开洋葱皮
                // response -> choices[0] -> message -> content
                String responseBody = response.body();
                ObjectNode jsonResponse = (ObjectNode) objectMapper.readTree(responseBody);
                String content = jsonResponse.get("choices").get(0).get("message").get("content").asText();

                // 把 AI 返回的 content (纯JSON字符串) 转成 Task 对象
                return objectMapper.readValue(content, Task.class);
            } else {
                System.err.println("AI 请求失败: " + response.body());
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}