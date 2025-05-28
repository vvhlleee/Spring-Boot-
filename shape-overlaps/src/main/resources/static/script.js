class ShapeVisualizer {
    constructor() {
        this.canvas = document.getElementById('shapeCanvas');
        this.ctx = this.canvas.getContext('2d');
        this.jsonDataElement = document.getElementById('jsonData');
        this.statsElement = document.getElementById('stats');
        this.currentShapes = null;
        this.currentResponseData = null;

        // 이벤트 리스너 설정
        document.getElementById('generateBtn').addEventListener('click', () => this.generateShapes());
        document.getElementById('showJsonBtn').addEventListener('click', () => this.showJsonModal());
        window.addEventListener('resize', () => this.updateCanvasSize());

        // 모달 외부 클릭 시 닫기
        document.getElementById('jsonModal').addEventListener('click', (e) => {
            if (e.target.id === 'jsonModal') {
                this.closeJsonModal();
            }
        });

        // ESC 키로 모달 닫기
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeJsonModal();
            }
        });

        // 초기 캔버스 크기 설정
        this.updateCanvasSize();
    }

    updateCanvasSize() {
        const container = document.getElementById('canvas-container');
        const width = Math.max(800, window.innerWidth - 100);
        const height = Math.max(500, window.innerHeight - 400);

        this.canvas.width = width;
        this.canvas.height = height;
        this.canvas.style.width = width + 'px';
        this.canvas.style.height = height + 'px';

        // 캔버스 크기가 변경되면 기존 도형들을 다시 그림
        if (this.currentShapes) {
            this.drawShapes(this.currentShapes);
        }
    }

    async generateShapes() {
        const radius = document.getElementById('radius').value;
        const count = document.getElementById('count').value;
        const edges = document.getElementById('edges').value;

        // API 요청 URL 생성
        const params = new URLSearchParams({
            Action: 'ShapesOverlaps',
            Width: this.canvas.width,
            Height: this.canvas.height,
            RadiusMax: radius,
            HowMany: count,
            MaxEdges: edges
        });

        try {
            // 로딩 표시
            this.ctx.fillStyle = '#f0f0f0';
            this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
            this.ctx.fillStyle = '#666';
            this.ctx.font = '20px Arial';
            this.ctx.textAlign = 'center';
            this.ctx.fillText('생성 중...', this.canvas.width / 2, this.canvas.height / 2);

            // API 호출
            const response = await fetch(`/api?${params.toString()}`);
            const data = await response.json();

            if (data.RES.STATUS === 200) {
                // 응답에서 도형 데이터 추출
                this.currentShapes = data.RES.RESULT;
                this.currentResponseData = data;

                // 도형들 그리기
                this.drawShapes(this.currentShapes);

                // 통계 정보 업데이트
                this.updateStats(this.currentShapes);

                // JSON 데이터 저장
                this.jsonDataElement.textContent = JSON.stringify(data, null, 2);
            } else {
                this.showError('오류 발생: ' + data.RES.STATUS_MSG);
                console.error(data);
            }
        } catch (error) {
            console.error('API 호출 오류:', error);
            this.showError('서버와 통신 중 오류가 발생했습니다.');
        }
    }

    showError(message) {
        this.ctx.fillStyle = '#ffffff';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        this.ctx.fillStyle = '#e74c3c';
        this.ctx.font = '20px Arial';
        this.ctx.textAlign = 'center';
        this.ctx.fillText(message, this.canvas.width / 2, this.canvas.height / 2);
    }

    drawShapes(shapesData) {
        // 캔버스 클리어
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        // 배경
        this.ctx.fillStyle = '#ffffff';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        // 모든 도형 그리기
        const shapes = shapesData.shapes;
        for (const shape of shapes) {
            this.drawShape(shape);
        }

        // 통계 정보 표시
        this.drawStatistics(shapesData);
    }

    drawShape(shape) {
        this.ctx.strokeStyle = '#2c3e50';
        this.ctx.fillStyle = shape.color;
        this.ctx.lineWidth = 2;

        if (shape.type === 'circle') {
            this.drawCircle(shape);
        } else if (shape.type === 'regularPolygon' || shape.type === 'irregularPolygon') {
            this.drawPolygon(shape);
        }

        // 도형 ID 표시 (작게)
        this.ctx.fillStyle = '#000000';
        this.ctx.font = '10px Arial';
        this.ctx.textAlign = 'center';
        const text = shape.id.substring(shape.id.lastIndexOf('_') + 1);
        this.ctx.fillText(text, shape.center.x, shape.center.y);
    }

    drawCircle(circle) {
        this.ctx.beginPath();
        this.ctx.arc(circle.center.x, circle.center.y, circle.radius, 0, 2 * Math.PI);
        this.ctx.fill();
        this.ctx.stroke();
    }

    drawPolygon(polygon) {
        const vertices = polygon.vertices;
        if (vertices.length < 3) return;

        this.ctx.beginPath();
        this.ctx.moveTo(vertices[0].x, vertices[0].y);

        for (let i = 1; i < vertices.length; i++) {
            this.ctx.lineTo(vertices[i].x, vertices[i].y);
        }

        this.ctx.closePath();
        this.ctx.fill();
        this.ctx.stroke();
    }

    drawStatistics(shapesData) {
        const { overlapGroups } = shapesData;

        // 겹침 그룹 정보 표시
        if (overlapGroups.length > 0) {
            this.ctx.font = '12px Arial';
            this.ctx.textAlign = 'left';
            let y = 20;

            overlapGroups.forEach((group, index) => {
                this.ctx.fillStyle = group.color;
                this.ctx.fillRect(10, y - 10, 15, 15);
                this.ctx.fillStyle = '#000000';
                this.ctx.fillText(`Group ${index + 1}: ${group.size} shapes`, 30, y);
                y += 20;
            });
        }
    }

    updateStats(shapesData) {
        const { shapes, totalCount, overlapGroups } = shapesData;

        // 도형 타입별 개수 계산
        const typeCount = {
            circle: 0,
            regularPolygon: 0,
            irregularPolygon: 0
        };

        shapes.forEach(shape => {
            typeCount[shape.type]++;
        });

        // 겹치는 도형 개수 계산
        const overlappingShapes = new Set();
        overlapGroups.forEach(group => {
            group.shapeIds.forEach(id => overlappingShapes.add(id));
        });

        // 통계 HTML 생성
        const statsHtml = `
            <div class="stat-item">
                <strong>Total Shapes:</strong> ${totalCount}
            </div>
            <div class="stat-item">
                <strong>Circles:</strong> ${typeCount.circle} (${(typeCount.circle/totalCount*100).toFixed(1)}%)
            </div>
            <div class="stat-item">
                <strong>Regular Polygons:</strong> ${typeCount.regularPolygon} (${(typeCount.regularPolygon/totalCount*100).toFixed(1)}%)
            </div>
            <div class="stat-item">
                <strong>Irregular Polygons:</strong> ${typeCount.irregularPolygon} (${(typeCount.irregularPolygon/totalCount*100).toFixed(1)}%)
            </div>
            <div class="stat-item">
                <strong>Overlap Groups:</strong> ${overlapGroups.length}
            </div>
            <div class="stat-item">
                <strong>Overlapping Shapes:</strong> ${overlappingShapes.size}
            </div>
        `;

        this.statsElement.innerHTML = statsHtml;
    }

    showJsonModal() {
        if (!this.currentResponseData) {
            alert('먼저 도형을 생성해주세요.');
            return;
        }

        document.getElementById('jsonModal').style.display = 'block';
    }

    closeJsonModal() {
        document.getElementById('jsonModal').style.display = 'none';
    }
}

// 전역 함수로 모달 닫기 함수 정의
function closeJsonModal() {
    document.getElementById('jsonModal').style.display = 'none';
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    const visualizer = new ShapeVisualizer();
    // 첫 번째 도형 자동 생성
    visualizer.generateShapes();
});