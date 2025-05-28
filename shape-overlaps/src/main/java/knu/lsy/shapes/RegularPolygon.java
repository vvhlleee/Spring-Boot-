package knu.lsy.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math; // Math 클래스 임포트 명시

public class RegularPolygon extends Shape {
    // Shape 추상 클래스에서 제거된 필드를 RegularPolygon 클래스에 추가
    private Point center; // 다각형의 중심
    private double radius; // 외접원의 반지름 (정점 계산에 사용)
    private int sides; // 변의 수
    private double rotationAngle; // 회전 각도 (라디안)
    private List<Point> vertices; // 계산된 정점 목록

    public RegularPolygon(Point center, double radius, int sides, double rotationAngle) {
        // Shape 클래스의 인자 없는 생성자 호출 (ID와 색상 초기화)
        super();

        // RegularPolygon에 특화된 필드 초기화 및 유효성 검사
        if (center == null) {
            throw new IllegalArgumentException("Polygon center cannot be null");
        }
        if (radius <= 0) {
            throw new IllegalArgumentException("Polygon radius must be positive");
        }
        if (sides < 3) {
            throw new IllegalArgumentException("Polygon must have at least 3 sides");
        }

        this.center = center;
        this.radius = radius;
        this.sides = sides;
        this.rotationAngle = rotationAngle;
        // 정점 생성은 모든 필드 초기화 후 수행
        this.vertices = generateVertices();
    }

    // Shape 추상 클래스의 getCenter() 메서드 구현
    @Override
    public Point getCenter() {
        return this.center;
    }

    // Shape 추상 클래스의 getRadius() 메서드 구현
    // 여기서는 외접원의 반지름을 반환합니다.
    @Override
    public double getRadius() {
        return this.radius;
    }

    // 정점 생성 메서드 (생성자에서 호출)
    private List<Point> generateVertices() {
        List<Point> points = new ArrayList<>();
        double angleStep = 2 * Math.PI / sides;

        for (int i = 0; i < sides; i++) {
            // 중심점, 반지름, 각도, 회전 정보를 사용하여 각 정점 계산
            double angle = angleStep * i + rotationAngle;
            double x = this.center.getX() + this.radius * Math.cos(angle);
            double y = this.center.getY() + this.radius * Math.sin(angle);
            points.add(new Point(x, y));
        }

        return points;
    }

    // TODO: 학생 과제 - 정다각형의 겹침 감지 알고리즘 구현
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

            List<Point> vertices1 = this.getVertices();
            List<Point> vertices2 = other.getVertices();

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

            // 변 벡터의 법선 벡터 ((-edgeY, edgeX) 또는 (edgeY, -edgeX))
            // 법선 벡터를 단위 벡터로 정규화하여 사용합니다.
            double normalX = -edgeY;
            double normalY = edgeX;

            double length = Math.sqrt(normalX * normalX + normalY * normalY);
            if (length > 1e-6) { // 길이가 0이 아닌 경우에만 정규화
                axes.add(new Point(normalX / length, normalY / length));
            }
            // 중복되는 축을 제거하는 로직을 추가하면 성능 향상 가능
        }
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
            return !(other.max < this.min || this.max < other.min);
        }
    }


    // JSON 정보를 반환하는 메서드 구현
    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", getShapeType()); // getShapeType() 메서드 사용
        json.put("id", id); // Shape 클래스에서 상속받은 id 사용
        json.put("center", this.center.toJSON()); // RegularPolygon의 center 사용
        json.put("radius", this.radius);       // RegularPolygon의 radius 사용
        json.put("sides", this.sides);
        json.put("rotationAngle", this.rotationAngle);
        json.put("color", color); // Shape 클래스에서 상속받은 color 사용

        JSONArray verticesArray = new JSONArray();
        for (Point vertex : this.vertices) { // RegularPolygon의 vertices 사용
            verticesArray.put(vertex.toJSON());
        }
        json.put("vertices", verticesArray);

        return json;
    }

    // 도형 타입을 문자열로 반환하는 메서드 구현
    @Override
    public String getShapeType() {
        return "regularPolygon";
    }

    // 정점 목록을 반환하는 메서드 구현
    @Override
    public List<Point> getVertices() {
        // 외부에서 정점을 수정할 수 없도록 새로운 목록을 반환
        return new ArrayList<>(this.vertices);
    }
}
