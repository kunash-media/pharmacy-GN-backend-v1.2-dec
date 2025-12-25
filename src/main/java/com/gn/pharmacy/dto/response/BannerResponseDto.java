package com.gn.pharmacy.dto.response;

import java.util.List;

public class BannerResponseDto {

    private Long bannerId;
    private String pageName;
    private List<String> bannerFileSlides;

    private String bannerFileTwo;
    private String bannerFileThree;
    private String bannerFileFour;

    public Long getId() {
        return bannerId;
    }

    public void setId(Long bannerId) {
        this.bannerId = bannerId;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public List<String> getBannerFileSlides() {
        return bannerFileSlides;
    }

    public void setBannerFileSlides(List<String> bannerFileSlides) {
        this.bannerFileSlides = bannerFileSlides;
    }

    public String getBannerFileTwo() {
        return bannerFileTwo;
    }

    public void setBannerFileTwo(String bannerFileTwo) {
        this.bannerFileTwo = bannerFileTwo;
    }

    public String getBannerFileThree() {
        return bannerFileThree;
    }

    public void setBannerFileThree(String bannerFileThree) {
        this.bannerFileThree = bannerFileThree;
    }

    public String getBannerFileFour() {
        return bannerFileFour;
    }

    public void setBannerFileFour(String bannerFileFour) {
        this.bannerFileFour = bannerFileFour;
    }
}