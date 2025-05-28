package knu.lsy.shapes;

import org.json.JSONArray; // 이 클래스는 현재 Circle에서 직접 사용되지 않습니다.
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class Circle extends Shape {
    // Shape 추상 클래스에서 제거된 center와 radius 필드를 Circle 클래스에 추가
    private Point center;
    private double radius;

    public Circle(Point center, double radius) {
        // Shape 클래스의 인자 없는 생성자 호출
        super();
        // Circle 클래스에 특화된 center와 radius 필드 초기화
        if (center == null) {
            throw new IllegalArgumentException("Circle center cannot be null");
        }
        if (radius < 0) {
            throw new IllegalArgumentException("Circle radius cannot be negative");
        }
        this.center = center;
        this.radius = radius;
    }

    // Shape 추상 클래스의 getCenter() 메서드 구현
    @Override
    public Point getCenter() {
        return this.center;
    }

    // Shape 추상 클래스의 getRadius() 메서드 구현
    @Override
    public double getRadius() {
        return this.radius;
    }

    // TODO: 학생 과제 - 원의 겹침 감지 알고리즘 구현 (이전 코드와 동일)
    @Override
    public boolean overlaps(Shape other) {
        // 겹침 감지 로직 구현 시작

        if (other instanceof Circle) {
            // 1. 다른 도형이 원인 경우 (원-원 겹침 검사)
            // 두 원의 중심 거리가 반지름의 합보다 작은지 확인
            Circle otherCircle = (Circle) other;
            // 다른 원의 중심과 반지름은 getCenter()와 getRadius()를 통해 접근
            double distanceBetweenCenters = this.getCenter().distanceTo(otherCircle.getCenter());
            double sumOfRadii = this.getRadius() + otherCircle.getRadius(); // getRadius() 사용

            // 두 원의 중심 거리가 반지름의 합보다 작거나 같으면 겹침
            // 부동 소수점 오차를 감안하여 아주 작은 epsilon 값을 사용하거나,
            // 여기서는 단순 비교로 구현합니다.
            return distanceBetweenCenters <= sumOfRadii;

        } else {
            // 2. 다른 도형이 다각형인 경우 (원-다각형 겹침 검사)
            // 다각형의 모든 정점이 원 안에 있는지 확인
            // 또는 다각형의 모든 변이 원과 교차하는지 확인

            // 다각형의 정점 목록을 가져옵니다.
            // Polygon 클래스들이 getVertices()를 올바르게 구현했다고 가정합니다.
            List<Point> vertices = other.getVertices();
            if (vertices == null || vertices.isEmpty()) {
                // 정점이 없으면 겹치지 않는다고 판단하거나 에러 처리
                // 여기서는 간단히 false 반환 (혹은 예외 처리 필요시 변경)
                return false;
            }

            // 원의 중심과 반지름 (getCenter(), getRadius() 사용)
            Point circleCenter = this.getCenter();
            double circleRadius = this.getRadius();

            // 2-1. 다각형의 어떤 정점이라도 원 안에 있는지 확인
            for (Point vertex : vertices) {
                if (circleCenter.distanceTo(vertex) <= circleRadius) {
                    // 정점이 원 안에 있으면 겹침
                    return true;
                }
            }

            // 2-2. 다각형의 어떤 변이라도 원과 교차하는지 확인
            // 각 변은 (vertices[i], vertices[i+1]) 또는 마지막 변 (vertices[last], vertices[0])
            int numVertices = vertices.size();
            for (int i = 0; i < numVertices; i++) {
                Point p1 = vertices.get(i);
                Point p2 = vertices.get((i + 1) % numVertices); // 마지막 정점과 첫 번째 정점을 연결

                // 원의 중심에서 변(선분)까지의 최소 거리를 계산하고 반지름과 비교
                if (isLineSegmentIntersectingCircle(circleCenter, circleRadius, p1, p2)) {
                    // 변이 원과 교차하면 겹침
                    return true;
                }
            }

            // 위 두 경우 모두 해당되지 않으면 겹치지 않음
            return false;
        }
    }

    /**
     * 원의 중심과 반지름, 그리고 선분의 양 끝점을 이용하여 원과 선분이 교차하는지 판별하는 헬퍼 메서드.
     * 이 메서드는 선분과 직선의 교차 여부 및 원의 중심에서 선분에 수선을 내렸을 때 수선의 발이 선분 안에 있는지 등을 고려합니다.
     *
     * @param circleCenter 원의 중심
     * @param circleRadius 원의 반지름
     * @param p1 선분의 시작점
     * @param p2 선분의 끝점
     * @return 원과 선분이 교차하면 true, 그렇지 않으면 false
     */
    private boolean isLineSegmentIntersectingCircle(Point circleCenter, double circleRadius, Point p1, Point p2) {
        // 선분의 벡터 (p2 - p1)
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();

        // 선분의 길이를 제곱
        double lenSq = dx * dx + dy * dy;

        // 만약 선분의 길이가 0이면 (p1과 p2가 같은 점) 단순히 그 점이 원 안에 있는지 확인
        if (lenSq == 0) {
            return circleCenter.distanceTo(p1) <= circleRadius;
        }

        // 원의 중심에서 p1까지의 벡터 (C - p1)
        double cx = circleCenter.getX() - p1.getX();
        double cy = circleCenter.getY() - p1.getY();

        // 원의 중심에서 선분에 내린 수선의 발이 선분 상에 있는지 판별하는 t 값 계산
        // t = dot(C - p1, p2 - p1) / dot(p2 - p1, p2 - p1)
        // t = dot(cx, cy, dx, dy) / lenSq
        double t = (cx * dx + cy * dy) / lenSq;

        // t 값을 0과 1 사이로 클램핑 (수선의 발이 선분 안에 있도록)
        // t = 0 이면 수선의 발이 p1, t = 1 이면 수선의 발이 p2
        // t가 0보다 작으면 p1 바깥쪽에, 1보다 크면 p2 바깥쪽에 수선의 발이 있음
        t = Math.max(0, Math.min(1, t));

        // 선분 상에서 원의 중심에 가장 가까운 점의 좌표 (closestPoint)
        double closestX = p1.getX() + t * dx;
        double closestY = p1.getY() + t * dy;
        Point closestPoint = new Point(closestX, closestY);

        // 원의 중심에서 가장 가까운 점까지의 거리가 원의 반지름보다 작거나 같으면 교차
        return circleCenter.distanceTo(closestPoint) <= circleRadius;
    }

    // 도형 정보를 JSON 형식으로 반환하는 메서드 구현
    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "circle");
        json.put("id", id); // Shape 클래스에서 상속받은 id 사용
        json.put("center", this.center.toJSON()); // Circle 클래스의 center 사용
        json.put("radius", this.radius);       // Circle 클래스의 radius 사용
        json.put("color", color); // Shape 클래스에서 상속받은 color 사용
        return json;
    }

    // 도형 타입을 문자열로 반환하는 메서드 구현
    @Override
    public String getShapeType() {
        return "circle";
    }

    // getVertices() 메서드 구현 (원에는 정점이 없으므로 빈 목록 반환)
    @Override
    public List<Point> getVertices() {
        return new ArrayList<>();
    }
}
