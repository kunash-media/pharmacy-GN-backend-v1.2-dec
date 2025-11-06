package com.gn.pharmacy.entity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "banners_table")
public class BannerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bannerId;

    @Column(name = "page_name")
    private String pageName;

    @ElementCollection
    @CollectionTable(name = "banner_file_slides", joinColumns = @JoinColumn(name = "banner_id"))
    @Lob
    @Column(name = "slide", columnDefinition = "LONGBLOB")
    private List<byte[]> bannerFileSlides = new ArrayList<>();

    @Lob
    @Column(name = "banner_file_two", columnDefinition = "LONGBLOB")
    private byte[] bannerFileTwo;

    @Lob
    @Column(name = "banner_file_three", columnDefinition = "LONGBLOB")
    private byte[] bannerFileThree;

    @Lob
    @Column(name = "banner_file_four", columnDefinition = "LONGBLOB")
    private byte[] bannerFileFour;


    public Long getBannerId() {
        return bannerId;
    }

    public void setBannerId(Long bannerId) {
        this.bannerId = bannerId;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public List<byte[]> getBannerFileSlides() {
        return bannerFileSlides;
    }

    public void setBannerFileSlides(List<byte[]> bannerFileSlides) {
        this.bannerFileSlides = bannerFileSlides;
    }

    public byte[] getBannerFileTwo() {
        return bannerFileTwo;
    }

    public void setBannerFileTwo(byte[] bannerFileTwo) {
        this.bannerFileTwo = bannerFileTwo;
    }

    public byte[] getBannerFileThree() {
        return bannerFileThree;
    }

    public void setBannerFileThree(byte[] bannerFileThree) {
        this.bannerFileThree = bannerFileThree;
    }

    public byte[] getBannerFileFour() {
        return bannerFileFour;
    }

    public void setBannerFileFour(byte[] bannerFileFour) {
        this.bannerFileFour = bannerFileFour;
    }
}
