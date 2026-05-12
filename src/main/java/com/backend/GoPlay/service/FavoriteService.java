package com.backend.GoPlay.service;

import com.backend.GoPlay.dto.court.CourtDetailResponse;
import com.backend.GoPlay.model.User;

import java.util.List;

public interface FavoriteService {
    /**
     * Thêm một sân vào danh sách yêu thích của người dùng.
     * @param courtId ID của sân cần thêm.
     * @param player Người dùng đang thực hiện hành động.
     */
    void addCourtToFavorites(Integer courtId, User player);

    /**
     * Xóa một sân khỏi danh sách yêu thích của người dùng.
     * @param courtId ID của sân cần xóa.
     * @param player Người dùng đang thực hiện hành động.
     */
    void removeCourtFromFavorites(Integer courtId, User player);

    /**
     * Lấy danh sách các sân yêu thích của người dùng.
     * @param player Người dùng đang đăng nhập.
     * @return Danh sách các sân yêu thích.
     */
    List<CourtDetailResponse> getFavoriteCourts(User player);
}
