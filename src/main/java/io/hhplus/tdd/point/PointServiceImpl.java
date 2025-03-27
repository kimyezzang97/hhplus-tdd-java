package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointServiceImpl implements PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointServiceImpl(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    // id로 포인트 조회
    public UserPoint selectById(Long id){
        // System.out.println(userPoint);
        return userPointTable.selectById(id);
    }

    // 포인트 충전 or 사용
    public UserPoint insertOrUpdate(Long id, Long point, TransactionType transactionType){
        if (point <= 0) throw new IllegalArgumentException("포인트는 0 이하로 충전 및 사용할 수 없습니다.");
        if (point > 10000000) throw new IllegalArgumentException("포인트는 1,000,000 초과로 충전 및 사용할 수 없습니다.");
        if (transactionType == TransactionType.CHARGE)
            return insert(id, point);
        return update(id, point);
    }

    // 포인트 충전
    private UserPoint insert(Long id, Long point){
        UserPoint userPoint = userPointTable.selectById(id);

        long totalPoint = userPoint.point() + point;
        if(totalPoint > 100000) throw new IllegalArgumentException("총 포인트는 1,000,000을 초과할 수 없습니다.");

        // 포인트 내역 추가
        pointHistoryTable.insert(id, point,TransactionType.CHARGE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(id, totalPoint);
    }

    // 포인트 사용
    private UserPoint update(Long id, Long point){
        if (point < 1000) throw new IllegalArgumentException("포인트는 1000 미만으로 사용할 수 없습니다.");

        UserPoint userPoint = userPointTable.selectById(id);
        long totalPoint = 0L;

        if(userPoint.point() - point < 0) throw new IllegalArgumentException("포인트 사용을 잔여포인트 보다 많이 사용할 수 없습니다.");
        totalPoint = userPoint.point() - point;

        // 포인트 내역 추가
        pointHistoryTable.insert(id, point,TransactionType.USE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(id, totalPoint);
    }

    // 포인트 내역 조회
    public List<PointHistory> selectHistory(Long id){
        return pointHistoryTable.selectAllByUserId(id);
    }
}
