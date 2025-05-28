package knu.lsy.shapes;

import org.json.JSONObject;
import java.util.List;
import java.util.UUID; // 고유 ID 생성을 위해 UUID 사용

public abstract class Shape {
    // 모든 도형이 공통적으로 가질 수 있는 속성
    protected String id;
    protected String color;

    // 도형 생성 시 기본적인 공통 속성 초기화
    public Shape() {
        this.id = generateId();
        this.color = generateRandomColor();
    }

    // 고유 ID 생성 메서드
    protected String generateId() {
        // 시스템 시간 대신 더 보편적인 UUID 사용
        return "shape_" + UUID.randomUUID().toString();
    }

    // 랜덤 색상 생성 메서드
    protected String generateRandomColor() {
        int r = (int)(Math.random() * 256);
        int g = (int)(Math.random() * 256);
        int b = (int)(Math.random() * 256);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public String getId() {
        return id;
    }

    // 도형의 중심점을 반환하는 추상 메서드
    // 원은 실제 중심, 다각형은 무게 중심 등을 계산하여 반환 가능
    public abstract Point getCenter();

    // 도형을 둘러싸는 경계원의 반지름 등 대표적인 크기 정보를 반환하는 추상 메서드
    // 원은 자신의 반지름, 다각형은 경계원의 반지름 등을 반환
    // (overlaps 메서드 구현 시 필요할 수 있음)
    public abstract double getRadius();

    // TODO: 학생 과제 - 이 메서드를 각 하위 클래스에 맞게 구현하세요
    // 다른 도형과의 겹침 여부를 판단하는 추상 메서드
    public abstract boolean overlaps(Shape other);

    // 도형 정보를 JSON 형식으로 반환하는 추상 메서드
    public abstract JSONObject toJSON();

    // 도형 타입을 문자열로 반환하는 추상 메서드 (예: "circle", "regular_polygon")
    public abstract String getShapeType();

    // 다각형의 경우 정점 목록을 반환하는 추상 메서드
    // 원의 경우 빈 목록이나 근사 정점 목록을 반환할 수 있음
    public abstract List<Point> getVertices();

    @Override
    public String toString() {
        // 기본적인 toString 구현. 하위 클래스에서 오버라이드하여 상세 정보 추가 가능.
        return String.format("%s(ID: %s, Color: %s)", getShapeType(), id, color);
    }
}
