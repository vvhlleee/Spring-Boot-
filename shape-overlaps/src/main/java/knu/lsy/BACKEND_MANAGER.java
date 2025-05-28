package knu.lsy;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import knu.lsy.shapes.ShapeGenerator;

import java.util.Date;

@Component
public class BACKEND_MANAGER {

    public static void EXEC_TASK(JSONObject jsonResponse) throws Exception {
        JSONObject reqJson = jsonResponse.getJSONObject("REQ");
        JSONObject resJson = jsonResponse.getJSONObject("RES");

        String action = reqJson.getString("Action");

        switch (action) {
            case "echo":
                resJson.put("RESULT", reqJson.toString());
                break;

            case "getCurrentTime":
                resJson.put("RESULT", new Date().toString());
                break;

            case "calculateSum":
                if (!reqJson.has("num1") || !reqJson.has("num2")) {
                    throw new Exception("num1과 num2 파라미터가 필요합니다.");
                }

                try {
                    int num1 = Integer.parseInt(reqJson.getString("num1"));
                    int num2 = Integer.parseInt(reqJson.getString("num2"));
                    int sum = num1 + num2;

                    resJson.put("RESULT", sum);
                } catch (NumberFormatException e) {
                    throw new Exception("num1과 num2는 유효한 숫자여야 합니다.");
                }
                break;

            case "getServerInfo":
                JSONObject serverInfo = new JSONObject();
                serverInfo.put("javaVersion", System.getProperty("java.version"));
                serverInfo.put("osName", System.getProperty("os.name"));
                serverInfo.put("osVersion", System.getProperty("os.version"));
                serverInfo.put("userDir", System.getProperty("user.dir"));

                resJson.put("RESULT", serverInfo);
                break;

            case "ShapesOverlaps":
                if (!reqJson.has("Width") || !reqJson.has("Height") ||
                        !reqJson.has("RadiusMax") || !reqJson.has("HowMany") ||
                        !reqJson.has("MaxEdges")) {
                    throw new Exception("필수 파라미터가 누락되었습니다.");
                }

                try {
                    int width = Integer.parseInt(reqJson.getString("Width"));
                    int height = Integer.parseInt(reqJson.getString("Height"));
                    int radiusMax = Integer.parseInt(reqJson.getString("RadiusMax"));
                    int howMany = Integer.parseInt(reqJson.getString("HowMany"));
                    int maxEdges = Integer.parseInt(reqJson.getString("MaxEdges"));

                    if (width <= 0 || height <= 0 || radiusMax <= 0 ||
                            howMany <= 0 || maxEdges < 3) {
                        throw new Exception("파라미터 값이 올바르지 않습니다.");
                    }

                    ShapeGenerator generator = new ShapeGenerator();
                    JSONObject shapesData = generator.generateShapes(width, height,
                            radiusMax, howMany, maxEdges);

                    resJson.put("RESULT", shapesData);

                } catch (NumberFormatException e) {
                    throw new Exception("숫자 파라미터 형식이 올바르지 않습니다.");
                }
                break;

            default:
                throw new Exception("지원하지 않는 Action입니다: " + action);
        }
    }
}