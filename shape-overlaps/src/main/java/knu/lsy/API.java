package knu.lsy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@RestController
public class API {
    @RequestMapping(value="/api", method = {RequestMethod.GET, RequestMethod.POST})
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public String requestParams(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");

        JSONObject params_JSON = new JSONObject();

        Map<String, String[]> ARGS_MAP = request.getParameterMap();
        if (ARGS_MAP != null && !ARGS_MAP.isEmpty()) {
            for (String STR_KEY : ARGS_MAP.keySet()) {
                if (ARGS_MAP.get(STR_KEY) != null && ARGS_MAP.get(STR_KEY).length > 0) {
                    params_JSON.put(STR_KEY, ARGS_MAP.get(STR_KEY)[0]);
                }
            }
        }

        JSONObject JSON_RES = new JSONObject()
                .put("STATUS", 200)
                .put("STATUS_MSG", "OK");

        if (!params_JSON.has("Action")) {
            JSON_RES.put("STATUS", 400);
            JSON_RES.put("STATUS_MSG", "Bad Request");
            JSON_RES.put("MESSAGE", "Action 파라미터가 필요합니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            JSON_RES.put("ACTION", params_JSON.getString("Action"));
        }

        JSONObject JSON_RESPONSE = new JSONObject();
        JSON_RESPONSE.put("REQ", params_JSON);
        JSON_RESPONSE.put("RES", JSON_RES);

        try {
            if (params_JSON.has("Action")) {
                BACKEND_MANAGER.EXEC_TASK(JSON_RESPONSE);
            }
        } catch (Exception e) {
            JSON_RES.put("STATUS", 500);
            JSON_RES.put("STATUS_MSG", "Internal Server Error");

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String[] STR_LINES = sw.toString().split("\r\n");
            JSONArray JSON_ARRAY_StackTrace = new JSONArray();
            for(String s : STR_LINES) {
                JSON_ARRAY_StackTrace.put(s);
            }
            JSON_RES.put("ERROR_MESSAGE", e.getMessage());
            JSON_RES.put("StackTrace", JSON_ARRAY_StackTrace);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return JSON_RESPONSE.toString();
    }
}