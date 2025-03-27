package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PointServiceTest {

    /**
     * 포인트 조회
     */
    @Test
    @DisplayName("ID로 회원을 조회할 때 없으면 회원을 만들어서 반환한다.")
    void ifNotExistSelectById() {
        // given
        long id = 1L;
        PointService pointService = new PointService(new UserPointTable());

         // when
        UserPoint result = pointService.selectById(id);

        // then
        assertThat(result.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("ID로 회원을 조회할 때 존재하면 그 회원정보를 가져온다.")
    void test(){
        // given
        long id = 2L;
        PointService pointService = new PointService(new UserPointTable());
        UserPoint userPoint = new UserPoint(id, 1000, System.currentTimeMillis());
        pointService.insertOrUpdate(userPoint);

        // when
        UserPoint result = pointService.selectById(id);

        // then : 기존 충전했던 1,000 포인트와 일치한다.
        assertThat(result.point()).isEqualTo(userPoint.point());
    }

}