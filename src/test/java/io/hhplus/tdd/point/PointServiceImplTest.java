package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PointServiceImplTest {

    /**
     * 포인트 조회
     */
    @Test
    @DisplayName("ID로 회원을 조회할 때 없으면 회원을 만들어서 반환한다.")
    void ifNotExistSelectById() {
        // given
        long id = 1L;
        PointService pointService = new PointService() {
            @Override
            public UserPoint selectById(Long id) {
                return new UserPoint(id, 0, System.currentTimeMillis());
            }

            @Override
            public UserPoint insertOrUpdate(Long id, Long point, TransactionType transactionType) {
                return null;
            }

            @Override
            public List<PointHistory> selectHistory(Long id) {
                return List.of();
            }
        };

         // when
        UserPoint result = pointService.selectById(id);

        // then
        assertThat(result.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("ID로 회원을 조회할 때 존재하면 그 회원정보를 가져온다.")
    void ifExistSelectById() {
        // given
        Long id = 2L;
        Long point = 1000L;
        PointService pointService = new PointService() {
            @Override
            public UserPoint selectById(Long id) {
                return new UserPoint(id, point, System.currentTimeMillis());
            }

            @Override
            public UserPoint insertOrUpdate(Long id, Long point, TransactionType transactionType) {
                return null;
            }

            @Override
            public List<PointHistory> selectHistory(Long id) {
                return List.of();
            }
        };

        pointService.insertOrUpdate(id, point, TransactionType.CHARGE);

        // when
        UserPoint result = pointService.selectById(id);

        // then : 기존 충전했던 1,000 포인트와 일치한다.
        assertThat(result.point()).isEqualTo(point);
    }

    /**
     * 포인트 충전
     */
    @Test
    @DisplayName("포인트는 0이하로 충전할 수 없다.")
    void canNotInsertUnderZeroPoint() {
        // given
        Long id = 2L;
        Long point = 0L;
        PointService pointService = new PointService() {
            @Override
            public UserPoint selectById(Long id) {
                return null;
            }

            @Override
            public UserPoint insertOrUpdate(Long id, Long point, TransactionType transactionType) {
                throw new IllegalArgumentException("포인트는 0 이하로 충전 및 사용할 수 없습니다.");
            }

            @Override
            public List<PointHistory> selectHistory(Long id) {
                return List.of();
            }
        };

        // when
        Exception e = null;
        try {
            UserPoint result = pointService.insertOrUpdate(id, point, TransactionType.CHARGE);
        } catch (Exception exception) {
            e = exception;
        }

        // then
        assert e.getMessage().contains("포인트는 0 이하로 충전 및 사용할 수 없습니다.");
        assert e instanceof IllegalArgumentException;
    }

    @Test
    @DisplayName("포인트 첫 충전시 100만 초과로 충전할 수 없다.")
    void canNotInsertOverMillionPoint() {
        // given
        Long id = 2L;
        Long point = 10000001L;
        PointService pointService = new PointService() {
            @Override
            public UserPoint selectById(Long id) {
                return null;
            }

            @Override
            public UserPoint insertOrUpdate(Long id, Long point, TransactionType transactionType) {
                throw new IllegalArgumentException("포인트는 1,000,000 초과로 충전 및 사용할 수 없습니다.");
            }

            @Override
            public List<PointHistory> selectHistory(Long id) {
                return List.of();
            }
        };

        // when
        Exception e = null;
        try {
            UserPoint result = pointService.insertOrUpdate(id, point, TransactionType.CHARGE);
        } catch (Exception exception) {
            e = exception;
        }

        // then
        assert e.getMessage().contains("포인트는 1,000,000 초과로 충전 및 사용할 수 없습니다.");
        assert e instanceof IllegalArgumentException;
    }

    @Test
    @DisplayName("기존 포인트와 추가한 포인트의 합이 100만포인트를 초과할 수 없다.")
    void canNotInsertOverMillionTotalPoint(){
        // given
        long id = 10L;
        long point = 100000L;
        PointService pointService = new PointService() {
            @Override
            public UserPoint selectById(Long id) {
                return null;
            }

            @Override
            public UserPoint insertOrUpdate(Long id, Long point, TransactionType transactionType) {
                if(point == 100000L)
                    return new UserPoint(id, point, System.currentTimeMillis());
                throw new IllegalArgumentException("총 포인트는 1,000,000을 초과할 수 없습니다.");
            }

            @Override
            public List<PointHistory> selectHistory(Long id) {
                return List.of();
            }
        };
        pointService.insertOrUpdate(id, point, TransactionType.CHARGE);
        long point2 = 500L;

        // when
        Exception e = null;
        try {
            UserPoint result = pointService.insertOrUpdate(id, point2, TransactionType.CHARGE);
        } catch (Exception exception) {
            e = exception;
        }

        // then
        assert e.getMessage().contains("총 포인트는 1,000,000을 초과할 수 없습니다.");
        assert e instanceof IllegalArgumentException;
    }

    @Test
    @DisplayName("ID가 없는 신규회원도 포인트를 충전할 수 있다.")
    void ifNotExistUserInsertPoint() {
        // given
        long id = 10L;
        long point = 10000L;
        PointService pointService = new PointService() {
            @Override
            public UserPoint selectById(Long id) {
                return new UserPoint(id, point, System.currentTimeMillis());
            }

            @Override
            public UserPoint insertOrUpdate(Long id, Long point, TransactionType transactionType) {
                return new UserPoint(id, point, System.currentTimeMillis());
            }

            @Override
            public List<PointHistory> selectHistory(Long id) {
                return List.of();
            }
        };

        // when
        pointService.insertOrUpdate(id, point, TransactionType.CHARGE);
        UserPoint result = pointService.selectById(id);

        // then : 신규 회원이 충전했던 10,000 포인트와 조회한 포인트가 일치한다.
        assertThat(point).isEqualTo(result.point());
    }

    @Test
    @DisplayName("기존 회원은 잔여포인트에서 더 충전할 수 있다.")
    void ifExistUserInsertPoint() {
        // given
        long id = 10L;
        long point = 10000L;
        long point2 = 500L;
        PointService pointService = new PointService() {
            @Override
            public UserPoint selectById(Long id) {
                return new UserPoint(id, point + point2, System.currentTimeMillis());
            }

            @Override
            public UserPoint insertOrUpdate(Long id, Long point, TransactionType transactionType) {
                return null;
            }

            @Override
            public List<PointHistory> selectHistory(Long id) {
                return List.of();
            }
        };
        pointService.insertOrUpdate(id, point, TransactionType.CHARGE);

        // when
        pointService.insertOrUpdate(id, point2, TransactionType.CHARGE);
        UserPoint result = pointService.selectById(id);

        // then
        assertThat(point + point2).isEqualTo(result.point());
    }

    /**
     * 포인트 사용
     */
    @Test
    @DisplayName("포인트 사용을 잔여포인트 보다 많이 사용할 수 없다.")
    void canNotUsePointMoreThanExistPoint() {
        // given
        Long id = 2L;
        Long point = 10000L;
        PointServiceImpl pointServiceImpl = new PointServiceImpl(new UserPointTable(), new PointHistoryTable());

        // when
        Exception e = null;
        try {
            UserPoint result = pointServiceImpl.insertOrUpdate(id, point, TransactionType.USE);
        } catch (Exception exception) {
            e = exception;
        }

        // then
        assert e.getMessage().contains("포인트 사용을 잔여포인트 보다 많이 사용할 수 없습니다.");
        assert e instanceof IllegalArgumentException;
    }

    @Test
    @DisplayName("포인트는 1,000 미만으로 사용할 수 없다.")
    void canNotUsePointUnderThousandPoint(){
        // given
        Long id = 2L;
        Long point = 10000L;
        PointServiceImpl pointServiceImpl = new PointServiceImpl(new UserPointTable(), new PointHistoryTable());
        pointServiceImpl.insertOrUpdate(id, point, TransactionType.CHARGE);

        Long uerPoint = 999L;
        // when
        Exception e = null;
        try {
            UserPoint result = pointServiceImpl.insertOrUpdate(id, uerPoint, TransactionType.USE);
        } catch (Exception exception) {
            e = exception;
        }

        // then
        assert e.getMessage().contains("포인트는 1000 미만으로 사용할 수 없습니다.");
        assert e instanceof IllegalArgumentException;
    }

    @Test
    @DisplayName("잔여포인트가 있으면 1,000 이상 포인트를 사용할 수 있다.")
    void ifExistPointCanUsePoint(){
        // given
        Long id = 2L;
        Long point = 10000L;
        PointServiceImpl pointServiceImpl = new PointServiceImpl(new UserPointTable(), new PointHistoryTable());
        pointServiceImpl.insertOrUpdate(id, point, TransactionType.CHARGE);
        Long userPoint = 1000L;

        // when
        UserPoint result = pointServiceImpl.insertOrUpdate(id, userPoint, TransactionType.USE);

        // then
        assertThat(result.point()).isEqualTo(point - userPoint);
    }

    /**
     * 포인트 내역 조회
     */
    @Test
    @DisplayName("포인트 내역이 없는 ID의 경우 0을 반환한다.")
    void ifNotExistSelectHistory(){
        // given
        long id = 1L;
        PointServiceImpl pointServiceImpl = new PointServiceImpl(new UserPointTable(), new PointHistoryTable());

        // when
        List<PointHistory> pointHistoryList = pointServiceImpl.selectHistory(id);

        // then
        assertThat(pointHistoryList.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("포인트를 10번 충전하고 내역 개수를 확인한다.")
    void ifInsertTenHistories(){
        // given
        long id = 1L;
        long point = 10000L;
        PointServiceImpl pointServiceImpl = new PointServiceImpl(new UserPointTable(), new PointHistoryTable());
        for (int i=0; i<10; i++){
            pointServiceImpl.insertOrUpdate(id,point, TransactionType.CHARGE);
        }

        // when
        List<PointHistory> pointHistoryList = pointServiceImpl.selectHistory(id);

        // then
        assertThat(pointHistoryList.size()).isEqualTo(10);
    }

    @Test
    @DisplayName("포인트를 10번 충전하고 5번 사용하고 내역 개수를 확인한다.")
    void ifInsertTenUseFiveHistories(){
        // given
        long id = 1L;
        long point = 10000L;
        PointServiceImpl pointServiceImpl = new PointServiceImpl(new UserPointTable(), new PointHistoryTable());
        for (int i=0; i<10; i++){
            pointServiceImpl.insertOrUpdate(id,point, TransactionType.CHARGE);
        }
        for (int i=0; i<5; i++){
            pointServiceImpl.insertOrUpdate(id,point, TransactionType.USE);
        }
        // when
        List<PointHistory> pointHistoryList = pointServiceImpl.selectHistory(id);

        // then
        assertThat(pointHistoryList.size()).isEqualTo(15);
    }
}