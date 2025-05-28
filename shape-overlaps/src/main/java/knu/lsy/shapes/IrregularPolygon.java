package knu.lsy.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.lang.Math; // Math 클래스 임포트 명시
import java.util.NoSuchElementException; // 정점이 없을 경우 예외 처리를 위해 임포트

public class IrregularPolygon extends Shape {
    // IrregularPolygon은 자체적으로 정점 목록을 관리합니다.
    private List<Point> vertices;

    // 생성 시 center와 radius는 정점 생성에만 사용됩니다.
    public IrregularPolygon(Point centerForGeneration, double radiusForGeneration, int numVertices) {
        // Shape 클래스의 인자 없는 생성자 호출 (ID와 색상 초기화)
        super();

        // 유효성 검사
        if (numVertices < 3) {
            throw new IllegalArgumentException("Irregular polygon must have at least 3 vertices");
        }
        if (centerForGeneration == null) {
            throw new IllegalArgumentException("Center point for generation cannot be null");
        }
        if (radiusForGeneration <= 0) {
            throw new IllegalArgumentException("Radius for generation must be positive");
        }


        // 주어진 정보로 정점 생성
        this.vertices = generateIrregularVertices(centerForGeneration, radiusForGeneration, numVertices);

        // 생성된 정점으로 실제 다각형을 나타내므로, 필요하다면 여기서 정점 목록의 유효성을 추가 검사할 수 있습니다.
        // (예: 정점이 너무 가깝거나 일직선 상에 있는 경우 등)
    }

    // 불규칙한 정점 목록을 생성하는 메서드
    private List<Point> generateIrregularVertices(Point centerForGeneration, double radiusForGeneration, int numVertices) {
        List<Point> points = new ArrayList<>();

        // 1. 무작위 각도로 점들 생성
        List<Double> angles = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            angles.add(Math.random() * 2 * Math.PI);
        }
        Collections.sort(angles); // 각도 순으로 정렬

        // 2. 각 점에 대해 무작위 반경 적용 (centerForGeneration과 radiusForGeneration 사용)
        for (int i = 0; i < numVertices; i++) {
            double angle = angles.get(i);
            // radiusForGeneration의 50% ~ 100% 범위 내에서 무작위 반경 적용
            double r = radiusForGeneration * (0.5 + Math.random() * 0.5);
            double x = centerForGeneration.getX() + r * Math.cos(angle);
            double y = centerForGeneration.getY() + r * Math.sin(angle);
            points.add(new Point(x, y));
        }

        // 간단한 컨벡스 헐 생성 (생성된 정점들을 사용하여 볼록 다각형 만듦)
        // 과제에서 컨벡스 헐 생성을 요구하므로 포함합니다.
        // 만약 비-컨벡스 다각형 처리가 필요하다면 이 부분은 제거하고 SAT 알고리즘을 비-컨벡스용으로 수정해야 합니다.
        return createSimpleConvexHull(points);
    }

    // 주어진 점 목록의 컨벡스 헐을 생성하는 메서드 (그레이엄 스캔 또는 모노톤 체인 등 활용)
    // 이 예시에서는 간단한 모노톤 체인 알고리즘을 사용합니다.
    private List<Point> createSimpleConvexHull(List<Point> points) {
        if (points.size() < 3) return new ArrayList<>(points); // 3개 미만 점은 다각형이 아님

        // 1. x 좌표 기준으로 정렬합니다. x가 같으면 y 기준으로 정렬합니다.
        List<Point> sortedPoints = new ArrayList<>(points);
        sortedPoints.sort(Comparator.comparingDouble(Point::getX).thenComparingDouble(Point::getY));

        List<Point> hull = new ArrayList<>();

        // 2. 하부 헐을 만듭니다.
        for (Point p : sortedPoints) {
            // 마지막 두 점과 현재 점 p가 만드는 회전 방향이 시계 방향(<=0)이면 가운데 점은 헐에 포함되지 않음
            while (hull.size() >= 2 && orientation(hull.get(hull.size() - 2),
                    hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }

        // 3. 상부 헐을 만듭니다.
        int lowerSize = hull.size(); // 하부 헐의 크기 (상부 헐 시작점 인덱스)
        // 정렬된 점들을 뒤에서부터 순회합니다.
        for (int i = sortedPoints.size() - 2; i >= 0; i--) {
            Point p = sortedPoints.get(i);
            // 마지막 두 점과 현재 점 p가 만드는 회전 방향이 시계 방향(<=0)이면 가운데 점은 헐에 포함되지 않음
            while (hull.size() > lowerSize && orientation(hull.get(hull.size() - 2),
                    hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }

        // 4. 마지막 점 제거 (처음 점과 같으므로 중복)
        if (hull.size() > 1) {
            // 시작점과 끝점이 같으므로 끝점을 제거합니다.
            // hull.get(0)과 hull.get(hull.size() - 1)이 같은 점일 경우
            if (hull.get(0).getX() == hull.get(hull.size() - 1).getX() &&
                    hull.get(0).getY() == hull.get(hull.size() - 1).getY()) {
                hull.remove(hull.size() - 1);
            }
        }


        return hull;
    }

    // 세 점 p, q, r의 방향(시계 방향, 반시계 방향, 일직선)을 판별하는 헬퍼 메서드
    // 결과 > 0: 반시계 방향
    // 결과 < 0: 시계 방향
    // 결과 = 0: 일직선
    private double orientation(Point p, Point q, Point r) {
        return (q.getX() - p.getX()) * (r.getY() - p.getY()) -
                (q.getY() - p.getY()) * (r.getX() - p.getX());
    }

    // Shape 추상 클래스의 getCenter() 메서드 구현
    // 불규칙 다각형의 무게 중심을 계산하여 반환합니다.
    @Override
    public Point getCenter() {
        if (vertices == null || vertices.isEmpty()) {
            // 정점이 없으면 중심점 계산 불가
            // 필요에 따라 null 반환 또는 예외 처리
            throw new NoSuchElementException("Cannot calculate center for a polygon with no vertices");
        }

        double sumX = 0;
        double sumY = 0;
        for (Point vertex : vertices) {
            sumX += vertex.getX();
            sumY += vertex.getY();
        }
        // 평균을 내어 무게 중심으로 사용
        return new Point(sumX / vertices.size(), sumY / vertices.size());
    }

    // Shape 추상 클래스의 getRadius() 메서드 구현
    // 무게 중심에서 가장 먼 정점까지의 거리를 대표적인 크기로 반환합니다.
    @Override
    public double getRadius() {
        if (vertices == null || vertices.isEmpty()) {
            // 정점이 없으면 반지름 계산 불가
            // 필요에 따라 0.0 반환 또는 예외 처리
            return 0.0;
        }

        Point center = getCenter(); // 계산된 무게 중심
        double maxDistanceSq = 0; // 최대 거리 제곱

        for (Point vertex : vertices) {
            double dx = vertex.getX() - center.getX();
            double dy = vertex.getY() - center.getY();
            double distSq = dx * dx + dy * dy;
            maxDistanceSq = Math.max(maxDistanceSq, distSq);
        }

        return Math.sqrt(maxDistanceSq); // 제곱근을 취하여 실제 거리 반환
    }


    // TODO: 학생 과제 - 일반 다각형의 겹침 감지 알고리즘 구현
    @Override
    public boolean overlaps(Shape other) {
        // 겹침 감지 로직 구현 시작

        if (other instanceof Circle) {
            // 1. 다른 도형이 원인 경우 (다각형-원 겹침 검사)
            // 원 클래스의 overlaps 메서드에 위임하여 처리합니다.
            // Circle.overlaps(this)를 호출하면, Circle 클래스가 원-다각형 겹침을 판별합니다.
            return other.overlaps(this);

        } else if (other instanceof RegularPolygon || other instanceof IrregularPolygon) {
            // 2. 다른 도형이 다각형인 경우 (다각형-다각형 겹침 검사)
            // SAT(Separating Axis Theorem) 알고리즘 사용
            // RegularPolygon에서 사용한 SAT 헬퍼 메서드를 재사용합니다.

            List<Point> vertices1 = this.getVertices();
            List<Point> vertices2 = other.getVertices();

            if (vertices1.isEmpty() || vertices2.isEmpty()) {
                return false; // 둘 중 하나라도 정점이 없으면 겹치지 않음
            }

            // 이 다각형과 다른 다각형의 분리 축들을 모두 가져옵니다.
            List<Point> axes1 = getSeparatingAxes(vertices1);
            List<Point> axes2 = getSeparatingAxes(vertices2);

            // 모든 축에 대해 투영 결과를 확인합니다.
            // 어느 한 축에서라도 투영 결과가 겹치지 않으면 두 도형은 겹치지 않습니다.
            if (!checkOverlapOnAxes(vertices1, vertices2, axes1)) {
                return false; // 첫 번째 다각형의 축들에서 분리 축을 찾음
            }
            if (!checkOverlapOnAxes(vertices1, vertices2, axes2)) {
                return false; // 두 번째 다각형의 축들에서 분리 축을 찾음
            }

            // 모든 축에서 투영 결과가 겹치면 두 도형은 겹칩니다.
            return true;

        } else {
            // 지원하지 않는 다른 도형 타입과의 겹침
            // 필요에 따라 예외를 던지거나 false를 반환할 수 있습니다.
            return false;
        }
    }

    // --- SAT 알고리즘 헬퍼 메서드 (RegularPolygon에서 복사 또는 재사용) ---

    // 두 다각형의 정점 목록과 축 목록을 받아 모든 축에 대해 겹침을 확인하는 헬퍼 메서드
    private boolean checkOverlapOnAxes(List<Point> vertices1, List<Point> vertices2, List<Point> axes) {
        for (Point axis : axes) {
            // 각 축에 대해 두 다각형을 투영합니다.
            Projection p1 = project(vertices1, axis);
            Projection p2 = project(vertices2, axis);

            // 투영된 구간이 겹치지 않으면 분리 축이 존재하고, 두 도형은 겹치지 않습니다.
            if (!p1.overlaps(p2)) {
                return false;
            }
        }
        // 모든 축에서 투영 구간이 겹치면 분리 축이 존재하지 않고, 두 도형은 겹칩니다.
        return true;
    }

    // 다각형의 정점 목록을 받아 분리 축 (각 변의 법선 벡터) 목록을 계산하는 헬퍼 메서드
    private List<Point> getSeparatingAxes(List<Point> vertices) {
        List<Point> axes = new ArrayList<>();
        int numVertices = vertices.size();

        for (int i = 0; i < numVertices; i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % numVertices); // 다음 정점 (마지막은 첫 번째와 연결)

            // 변 벡터 (p2 - p1)
            double edgeX = p2.getX() - p1.getX();
            double edgeY = p2.getY() - p1.getY();

            // 변 벡터의 법선 벡터 ((-edgeY, edgeX))
            double normalX = -edgeY;
            double normalY = edgeX;

            // 법선 벡터를 단위 벡터로 정규화합니다.
            double length = Math.sqrt(normalX * normalX + normalY * normalY);
            // 아주 작은 값보다 큰 경우에만 정규화하고 추가 (부동 소수점 오차 방지)
            if (length > 1e-9) { // 0에 가까운 길이 필터링
                Point normal = new Point(normalX / length, normalY / length);
                // 중복되는 축을 방지하기 위한 간단한 체크 (방향 무관한 중복)
                boolean exists = false;
                for (Point existingAxis : axes) {
                    // 방향이 같거나 반대인 경우 중복으로 간주
                    if ((Math.abs(existingAxis.getX() - normal.getX()) < 1e-9 && Math.abs(existingAxis.getY() - normal.getY()) < 1e-9) ||
                            (Math.abs(existingAxis.getX() + normal.getX()) < 1e-9 && Math.abs(existingAxis.getY() + normal.getY()) < 1e-9)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    axes.add(normal);
                }
            }
        }
        // 모든 축이 중복 제거된 최종 축 목록 반환
        return axes;
    }

    // 다각형의 정점 목록과 축 벡터를 받아 해당 축에 투영된 구간을 계산하는 헬퍼 메서드
    private Projection project(List<Point> vertices, Point axis) {
        if (vertices.isEmpty()) {
            return new Projection(Double.MAX_VALUE, Double.MIN_VALUE); // 빈 구간
        }

        // 첫 번째 정점을 투영하여 초기 최소/최대값 설정
        double projection = dotProduct(vertices.get(0), axis);
        double min = projection;
        double max = projection;

        // 나머지 정점들을 투영하여 최소/최대값 업데이트
        for (int i = 1; i < vertices.size(); i++) {
            projection = dotProduct(vertices.get(i), axis);
            min = Math.min(min, projection);
            max = Math.max(max, projection);
        }

        return new Projection(min, max);
    }

    // 두 점(벡터)의 내적을 계산하는 헬퍼 메서드
    private double dotProduct(Point p1, Point p2) {
        return p1.getX() * p2.getX() + p1.getY() * p2.getY();
    }

    // 투영된 구간을 나타내는 내부 클래스
    private static class Projection {
        double min;
        double max;

        Projection(double min, double max) {
            this.min = min;
            this.max = max;
        }

        // 두 투영 구간이 겹치는지 확인
        boolean overlaps(Projection other) {
            // 한 구간의 최대값이 다른 구간의 최소값보다 작거나,
            // 다른 구간의 최대값이 이 구간의 최소값보다 작으면 겹치지 않음
            // 그 외의 경우는 겹침
            // 부동 소수점 오차를 고려하여 아주 작은 값(epsilon)을 더하거나 빼서 비교할 수 있습니다.
            // 여기서는 단순 비교로 구현합니다.
            return !(other.max < this.min || this.max < other.min);
        }
    }
    // --- SAT 알고리즘 헬퍼 메서드 끝 ---


    // JSON 정보를 반환하는 메서드 구현
    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", getShapeType()); // getShapeType() 메서드 사용
        json.put("id", id); // Shape 클래스에서 상속받은 id 사용
        // 실제 계산된 중심점과 대표 반지름 사용
        try {
            json.put("center", getCenter().toJSON());
            json.put("radius", getRadius());
        } catch (NoSuchElementException e) {
            // 정점이 없어 중심/반지름 계산이 불가능한 경우
            json.put("center", JSONObject.NULL); // 또는 적절한 기본값
            json.put("radius", 0.0); // 또는 적절한 기본값
            System.err.println("Warning: Could not calculate center/radius for IrregularPolygon with ID " + id + ": " + e.getMessage());
        }

        json.put("color", color); // Shape 클래스에서 상속받은 color 사용

        JSONArray verticesArray = new JSONArray();
        if (this.vertices != null) {
            for (Point vertex : this.vertices) { // IrregularPolygon의 vertices 사용
                verticesArray.put(vertex.toJSON());
            }
        }
        json.put("vertices", verticesArray);

        return json;
    }

    // 도형 타입을 문자열로 반환하는 메서드 구현
    @Override
    public String getShapeType() {
        return "irregularPolygon";
    }

    // 정점 목록을 반환하는 메서드 구현
    @Override
    public List<Point> getVertices() {
        // 외부에서 정점을 수정할 수 없도록 새로운 목록을 반환
        if (this.vertices == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(this.vertices);
    }
}
