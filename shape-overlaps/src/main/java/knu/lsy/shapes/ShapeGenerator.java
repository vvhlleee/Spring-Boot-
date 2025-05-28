package knu.lsy.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;
import java.lang.Math; // Math 클래스 사용을 위해 임포트

public class ShapeGenerator {
    private Random random;

    // 겹침 그룹에 할당할 색상 배열 (클래스 상수)
    private static final String[] COLORS = {
            "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF",
            "#00FFFF", "#FFA500", "#800080", "#008000", "#000080",
            "#FFC0CB", "#E6E6FA", "#FFFACD", "#90EE90", "#ADD8E6"
            // 필요에 따라 더 많은 색상 추가 가능
    };

    public ShapeGenerator() {
        this.random = new Random();
    }

    /**
     * 지정된 영역 안에 다양한 종류의 도형을 무작위로 생성하고,
     * 겹치는 도형들을 그룹화하여 그룹별로 색상을 지정한 후 JSON 형식으로 반환합니다.
     *
     * @param width 생성 영역의 너비
     * @param height 생성 영역의 높이
     * @param radiusMax 도형의 최대 반지름 (또는 생성에 사용될 최대 크기 기준)
     * @param howMany 생성할 도형의 총 개수
     * @param maxEdges 다각형의 최대 변(정점) 수
     * @return 생성된 도형 목록 및 겹침 그룹 정보를 포함하는 JSONObject
     */
    public JSONObject generateShapes(int width, int height, int radiusMax, int howMany, int maxEdges) {
        List<Shape> shapes = new ArrayList<>();

        // 도형 생성 (원: 20%, 정다각형: 25%, 일반다각형: 55% - 예시 비율)
        for (int i = 0; i < howMany; i++) {
            double probability = random.nextDouble();

            // 무작위 중심점 생성 (영역 내에 완전히 포함되도록 radiusMax 고려)
            // 이 center는 도형 생성자의 인자로 전달되어 각 도형 클래스에서 자신의 중심이나 정점 생성 기준으로 사용됩니다.
            double centerX = radiusMax + random.nextDouble() * (width - 2 * radiusMax);
            double centerY = radiusMax + random.nextDouble() * (height - 2 * radiusMax);
            Point centerForGeneration = new Point(centerX, centerY);

            // 무작위 반경 (도형 크기 결정에 사용될 값)
            // 이 radius는 도형 생성자의 인자로 전달되어 각 도형 클래스에서 자신의 반지름이나 크기 기준으로 사용됩니다. 최소 반지름 10으로 설정
            double radiusForGeneration = 10 + random.nextDouble() * (radiusMax - 10);

            Shape shape;
            // 확률에 따라 도형 종류 결정 및 생성
            if (probability < 0.20) {
                // Circle 생성자: Point center, double radius
                shape = new Circle(centerForGeneration, radiusForGeneration);
            } else if (probability < 0.45) {
                // RegularPolygon 생성자: Point center, double radius, int sides, double rotationAngle
                int sides = 3 + random.nextInt(maxEdges - 2); // 3변 이상 maxEdges변 이하
                double rotation = random.nextDouble() * 2 * Math.PI; // 0 ~ 2pi 라디안 회전
                shape = new RegularPolygon(centerForGeneration, radiusForGeneration, sides, rotation);
            } else {
                // IrregularPolygon 생성자: Point centerForGeneration, double radiusForGeneration, int numVertices
                int numVertices = 3 + random.nextInt(maxEdges - 2); // 3개 이상 maxEdges개 이하 정점
                shape = new IrregularPolygon(centerForGeneration, radiusForGeneration, numVertices);
            }

            shapes.add(shape);
        }

        // 연쇄적 그룹화 처리 (Union-Find 활용)
        List<Set<String>> overlapGroups = findConnectedComponents(shapes);
        assignGroupColors(shapes, overlapGroups); // 겹치는 그룹별 색상 지정

        // JSON 응답 생성
        JSONObject response = new JSONObject();
        JSONArray shapesArray = new JSONArray();

        // 생성된 모든 도형 정보를 JSON 배열에 추가
        for (Shape shape : shapes) {
            shapesArray.put(shape.toJSON()); // 각 도형의 toJSON() 호출
        }

        response.put("shapes", shapesArray); // 도형 목록
        response.put("totalCount", shapes.size()); // 총 도형 개수
        response.put("overlapGroups", convertGroupsToJSON(overlapGroups)); // 겹침 그룹 정보 (JSON 배열)

        return response;
    }

    /**
     * Union-Find 자료구조를 사용하여 주어진 도형 목록에서 겹치는 도형들로 연결된 구성 요소(그룹)를 찾습니다.
     *
     * @param shapes 겹침 여부를 확인할 도형 목록
     * @return 각 겹침 그룹에 속한 도형 ID들의 집합(Set) 목록
     */
    private List<Set<String>> findConnectedComponents(List<Shape> shapes) {
        // Union-Find 자료구조를 위한 맵 초기화
        Map<String, String> parent = new HashMap<>(); // 각 원소의 부모를 저장
        Map<String, Integer> rank = new HashMap<>(); // 트리의 높이 또는 크기를 나타내는 랭크 (Union 최적화용)

        // 각 도형의 ID를 독립적인 Union-Find 집합으로 초기화
        for (Shape shape : shapes) {
            parent.put(shape.getId(), shape.getId()); // 처음에는 자기 자신을 대표(루트) 원소로 설정
            rank.put(shape.getId(), 0); // 초기 랭크는 0
        }

        // 모든 도형 쌍에 대해 겹침 검사를 수행하고 겹치면 두 도형이 속한 집합을 합칩니다 (Union 연산)
        for (int i = 0; i < shapes.size(); i++) {
            for (int j = i + 1; j < shapes.size(); j++) {
                // shapes.get(i).overlaps(shapes.get(j))를 호출하여 겹침 여부 확인
                if (shapes.get(i).overlaps(shapes.get(j))) {
                    // 겹치면 두 도형의 ID가 속한 집합을 Union 합니다.
                    union(parent, rank, shapes.get(i).getId(), shapes.get(j).getId());
                }
            }
        }

        // Union 연산이 완료된 후, 각 도형이 속한 최종 루트(대표 원소)를 기준으로 그룹화합니다.
        Map<String, Set<String>> groupMap = new HashMap<>();
        for (Shape shape : shapes) {
            String root = find(parent, shape.getId()); // 해당 도형이 속한 집합의 대표 원소 찾기
            // computeIfAbsent: 해당 키(root)가 맵에 없으면 새로운 HashSet을 만들고, 있으면 기존 Set을 가져와 도형 ID를 추가
            groupMap.computeIfAbsent(root, k -> new HashSet<>()).add(shape.getId());
        }

        // 그룹 맵의 값(도형 ID 집합 목록)을 List 형태로 반환
        return new ArrayList<>(groupMap.values());
    }

    /**
     * Union-Find의 Find 연산 (경로 압축 최적화 적용): 특정 원소가 속한 집합의 대표 원소를 찾습니다.
     * 재귀적으로 부모를 따라가면서 루트를 찾고, 그 과정에서 만나는 모든 노드의 부모를 루트로 직접 연결하여 경로를 압축합니다.
     *
     * @param parent 부모 정보를 담고 있는 맵
     * @param x 대표 원소를 찾을 원소의 ID
     * @return 원소 x가 속한 집합의 대표 원소 ID
     */
    private String find(Map<String, String> parent, String x) {
        // 기저 조건: 현재 원소 x가 자신의 부모와 같다면, x는 루트입니다.
        if (!parent.get(x).equals(x)) {
            // 현재 원소가 루트가 아니라면, 부모의 Find를 재귀적으로 호출하고
            // 그 결과(찾아낸 루트)를 현재 원소의 새로운 부모로 설정하여 경로를 압축합니다.
            parent.put(x, find(parent, parent.get(x)));
        }
        // 압축된 경로를 통해 최종적으로 찾아낸 루트를 반환합니다.
        return parent.get(x);
    }

    /**
     * Union-Find의 Union 연산 (랭크 기반 합치기 적용): 두 원소가 속한 집합을 합칩니다.
     * 두 트리를 합칠 때, 랭크가 낮은 트리를 랭크가 높은 트리의 자식으로 만들어 트리의 깊이 증가를 최소화합니다.
     * 랭크가 같으면 둘 중 하나의 랭크를 증가시킵니다.
     *
     * @param parent 부모 정보를 담고 있는 맵
     * @param rank 트리의 랭크 정보를 담고 있는 맵
     * @param x 합칠 첫 번째 원소의 ID
     * @param y 합칠 두 번째 원소의 ID
     */
    private void union(Map<String, String> parent, Map<String, Integer> rank,
                       String x, String y) {
        // 각 원소가 속한 집합의 대표 원소(루트)를 찾습니다.
        String rootX = find(parent, x);
        String rootY = find(parent, y); // 오타 수정 완료

        // 두 원소가 이미 같은 집합에 속해 있지 않다면 (대표 원소가 다르면)
        if (!rootX.equals(rootY)) {
            // 랭크가 낮은 트리를 랭크가 높은 트리에 붙입니다. (랭크 기반 합치기)
            if (rank.get(rootX) < rank.get(rootY)) {
                parent.put(rootX, rootY); // rootX 집합을 rootY 집합 아래로 합침
            } else if (rank.get(rootX) > rank.get(rootY)) {
                parent.put(rootY, rootX); // rootY 집합을 rootX 집합 아래로 합침
            } else {
                // 랭크가 같으면 둘 중 하나 (여기서는 rootY)를 다른 하나 (rootX)의 자식으로 만들고
                // 부모가 된 트리의 랭크를 1 증가시킵니다.
                parent.put(rootY, rootX);
                rank.put(rootX, rank.get(rootX) + 1);
            }
        }
    }

    /**
     * 겹침 그룹별로 고유한 색상을 할당하고, 해당 그룹에 속한 도형들의 색상을 업데이트합니다.
     * 그룹 크기가 1인 경우는 (단일 도형) 색상을 변경하지 않고 기본 색상(랜덤)을 유지합니다.
     *
     * @param shapes 전체 도형 목록
     * @param groups 겹침 그룹 목록 (도형 ID 집합)
     */
    private void assignGroupColors(List<Shape> shapes, List<Set<String>> groups) {
        // 도형 ID로 Shape 객체를 빠르게 찾기 위한 맵 생성
        Map<String, Shape> shapeMap = new HashMap<>();
        for (Shape shape : shapes) {
            shapeMap.put(shape.getId(), shape);
        }

        // 각 그룹에 대해 색상을 할당하고 해당 그룹의 도형 색상을 업데이트
        for (int i = 0; i < groups.size(); i++) {
            Set<String> group = groups.get(i);
            // 그룹에 속한 도형이 2개 이상인 경우에만 색상 할당
            if (group.size() > 1) {
                // 미리 정의된 색상 배열에서 그룹 인덱스를 사용하여 색상 선택 (순환)
                String color = COLORS[i % COLORS.length];
                for (String shapeId : group) {
                    Shape shape = shapeMap.get(shapeId);
                    // ID에 해당하는 도형 객체를 찾아서 색상 업데이트
                    if (shape != null) {
                        shape.setColor(color);
                    }
                }
            }
        }
    }

    /**
     * 겹침 그룹 목록을 JSON 배열 형식으로 변환합니다.
     * 각 그룹은 해당 그룹에 속한 도형 ID들의 JSON 배열로 표현되며, 그룹의 색상과 크기 정보도 포함됩니다.
     * 그룹 크기가 1인 경우는 JSON 결과에 포함되지 않습니다.
     *
     * @param groups 겹침 그룹 목록 (도형 ID 집합)
     * @return 겹침 그룹 정보를 담고 있는 JSONArray (각 그룹은 JSONObject)
     */
    private JSONArray convertGroupsToJSON(List<Set<String>> groups) {
        JSONArray groupsArray = new JSONArray();

        // 각 그룹 정보를 JSON 객체로 만들어 배열에 추가
        for (int i = 0; i < groups.size(); i++) {
            Set<String> group = groups.get(i);
            // 그룹에 속한 도형이 2개 이상인 경우에만 그룹 정보를 JSON에 추가
            if (group.size() > 1) {
                JSONObject groupJson = new JSONObject();
                JSONArray shapeIds = new JSONArray();

                // 그룹에 속한 모든 도형 ID를 JSON 배열에 추가
                for (String shapeId : group) {
                    shapeIds.put(shapeId);
                }

                groupJson.put("shapeIds", shapeIds); // 그룹 내 도형 ID 목록
                // assignGroupColors에서 사용한 색상 로직과 동일하게 색상 할당
                String color = COLORS[i % COLORS.length];
                groupJson.put("color", color); // 그룹 색상
                groupJson.put("size", group.size()); // 그룹 크기 (도형 개수)

                groupsArray.put(groupJson); // 완성된 그룹 JSON 객체를 배열에 추가
            }
        }

        return groupsArray; // 최종 그룹 JSON 배열 반환
    }
}
