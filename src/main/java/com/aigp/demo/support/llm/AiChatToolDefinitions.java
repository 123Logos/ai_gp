package com.aigp.demo.support.llm;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AiChatToolDefinitions {

	private AiChatToolDefinitions() {}

	public static List<Map<String, Object>> taskTools() {
		return List.of(
				tool(
						"list_tasks",
						"查询当前用户的任务列表，可按状态与截止日期范围筛选",
						Map.of(
								"type",
								"object",
								"properties",
								Map.of(
										"status",
										Map.of(
												"type",
												"string",
												"enum",
												List.of("OPEN", "DONE", "CANCELLED"),
												"description",
												"可选，按状态筛选"),
										"dueFrom",
										Map.of(
												"type",
												"string",
												"description",
												"可选，截止日期下限 yyyy-MM-dd（含）"),
										"dueTo",
										Map.of(
												"type",
												"string",
												"description",
												"可选，截止日期上限 yyyy-MM-dd（含）")),
								"required",
								List.of())),
				tool(
						"get_task",
						"按任务 ID 查询单条任务详情",
						Map.of(
								"type",
								"object",
								"properties",
								Map.of("taskId", Map.of("type", "integer", "description", "任务 ID")),
								"required",
								List.of("taskId"))),
				tool(
						"create_task",
						"为用户创建一条新任务",
						Map.of(
								"type",
								"object",
								"properties",
								Map.of(
										"title",
										Map.of("type", "string", "description", "任务标题，必填"),
										"description",
										Map.of("type", "string", "description", "任务描述，可选"),
										"dueDate",
										Map.of(
												"type",
												"string",
												"description",
												"仅日期时填 yyyy-MM-dd；若已填 dueAt 可省略"),
										"dueAt",
										Map.of(
												"type",
												"string",
												"description",
												"截止时刻 yyyy-MM-dd HH:mm（用户本地，精确到分），有具体几点时用此项"),
										"imageAssetIds",
										Map.of(
												"type",
												"array",
												"items",
												Map.of("type", "integer"),
												"description",
												"可选；仅当用户明确要求把图片记入任务时传入 assetId")),
								"required",
								List.of("title"))),
				tool(
						"update_task",
						"更新用户已有任务（部分字段）",
						Map.of(
								"type",
								"object",
								"properties",
								Map.of(
										"taskId",
										Map.of("type", "integer", "description", "任务 ID"),
										"title",
										Map.of("type", "string"),
										"description",
										Map.of("type", "string"),
										"status",
										Map.of("type", "string", "enum", List.of("OPEN", "DONE", "CANCELLED")),
										"dueDate",
										Map.of("type", "string", "description", "yyyy-MM-dd；空字符串清除日期"),
										"dueAt",
										Map.of(
												"type",
												"string",
												"description",
												"yyyy-MM-dd HH:mm；空字符串清除时刻"),
										"imageAssetIds",
										Map.of(
												"type",
												"array",
												"items",
												Map.of("type", "integer"),
												"description",
												"替换任务附图；传空数组 [] 清除；不传则不改图片")),
								"required",
								List.of("taskId"))),
				tool(
						"delete_task",
						"删除（取消）用户的一条任务，将状态置为 CANCELLED",
						Map.of(
								"type",
								"object",
								"properties",
								Map.of("taskId", Map.of("type", "integer", "description", "任务 ID")),
								"required",
								List.of("taskId"))));
	}

	private static Map<String, Object> tool(String name, String description, Map<String, Object> parameters) {
		Map<String, Object> fn = new LinkedHashMap<>();
		fn.put("name", name);
		fn.put("description", description);
		fn.put("parameters", parameters);
		Map<String, Object> tool = new LinkedHashMap<>();
		tool.put("type", "function");
		tool.put("function", fn);
		return tool;
	}
}
