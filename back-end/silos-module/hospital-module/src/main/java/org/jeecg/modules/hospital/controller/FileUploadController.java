package org.jeecg.modules.hospital.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * 文件上传接口：/file/upload
 * 供校医院小程序身份认证照片、医生头像等业务统一使用
 */
@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Value("${jeecg.path.upload}")
    private String uploadpath;

    /**
     * 本地：local minio：minio 阿里：alioss
     */
    @Value("${jeecg.uploadType}")
    private String uploadType;

    @Value("${jeecg.domainUrl:http://127.0.0.1:8095}")
    private String domainUrl;

    // 最大文件大小 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @PostMapping("/upload")
    public ResponseEntity<HashMap<String, Object>> upload(HttpServletRequest request) {
        HashMap<String, Object> result = new HashMap<>();
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multipartRequest.getFile("file");
            if (file == null || file.isEmpty()) {
                result.put("code", 400);
                result.put("message", "文件不能为空");
                return ResponseEntity.ok(result);
            }

            // 验证文件大小（5MB限制）
            if (file.getSize() > MAX_FILE_SIZE) {
                result.put("code", 400);
                result.put("message", "文件大小不能超过5MB");
                return ResponseEntity.ok(result);
            }

            // 验证图片格式（仅允许 jpg, jpeg, png）
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                String lowerCaseFilename = originalFilename.toLowerCase();
                if (!lowerCaseFilename.endsWith(".jpg") && 
                    !lowerCaseFilename.endsWith(".jpeg") && 
                    !lowerCaseFilename.endsWith(".png")) {
                    result.put("code", 400);
                    result.put("message", "仅支持JPG和PNG格式的图片");
                    return ResponseEntity.ok(result);
                }
            }

            // 支持前端通过 biz 区分不同业务（identity、doctor-avatar 等）
            String bizPath = request.getParameter("biz");
            if (oConvertUtils.isEmpty(bizPath)) {
                // 默认身份认证
                bizPath = "identity";
            }

            // 防止目录穿越
            if (bizPath.contains(SymbolConstant.SPOT_SINGLE_SLASH) || bizPath.contains(SymbolConstant.SPOT_DOUBLE_BACKSLASH)) {
                throw new JeecgBootException("上传目录bizPath，格式非法！");
            }

            String url;
            if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)) {
                // 本地存储模式：参考系统 CommonController.uploadLocal 的实现
                url = this.uploadLocal(file, bizPath);
            } else {
                // 其它模式（minio/oss），沿用 jeecg 的统一上传逻辑
                url = CommonUtils.upload(file, bizPath, uploadType);
            }

            if (oConvertUtils.isEmpty(url)) {
                result.put("code", 500);
                result.put("message", "上传失败，请稍后重试");
                return ResponseEntity.ok(result);
            }

            HashMap<String, Object> data = new HashMap<>();
            data.put("url", url);

            result.put("code", 200);
            result.put("message", "上传成功");
            result.put("data", data);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            HashMap<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "系统异常，请稍后重试");
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 本地文件上传，实现参考系统 CommonController.uploadLocal
     */
    private String uploadLocal(MultipartFile mf, String bizPath) {
        try {
            // 过滤上传文件类型
            SsrfFileTypeFilter.checkUploadFileType(mf);

            String ctxPath = uploadpath;
            String fileName;
            File file = new File(ctxPath + File.separator + bizPath + File.separator);
            if (!file.exists()) {
                // 创建文件根目录
                file.mkdirs();
            }
            // 获取文件名
            String orgName = mf.getOriginalFilename();
            orgName = CommonUtils.getFileName(orgName);
            if (orgName.indexOf(SymbolConstant.SPOT) != -1) {
                fileName = orgName.substring(0, orgName.lastIndexOf(SymbolConstant.SPOT))
                        + "_" + System.currentTimeMillis()
                        + orgName.substring(orgName.lastIndexOf(SymbolConstant.SPOT));
            } else {
                fileName = orgName + "_" + System.currentTimeMillis();
            }
            String savePath = file.getPath() + File.separator + fileName;
            File savefile = new File(savePath);
            FileCopyUtils.copy(mf.getBytes(), savefile);
            String dbpath;
            if (oConvertUtils.isNotEmpty(bizPath)) {
                dbpath = bizPath + File.separator + fileName;
            } else {
                dbpath = fileName;
            }
            if (dbpath.contains(SymbolConstant.DOUBLE_BACKSLASH)) {
                dbpath = dbpath.replace(SymbolConstant.DOUBLE_BACKSLASH, SymbolConstant.SINGLE_SLASH);
            }
            return dbpath;
        } catch (IOException e) {
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取静态图片的完整URL
     * 用于前端通过API获取静态资源URL，替代直接使用/static/路径
     * 
     * @param filename 文件名，例如 "card.svg" 或 "images/no_data.png"
     * @return 完整的图片URL
     */
    @GetMapping("/static/{filename:.+}")
    public ResponseEntity<HashMap<String, Object>> getStaticImageUrl(@PathVariable String filename) {
        HashMap<String, Object> result = new HashMap<>();
        try {
            // 防止路径穿越攻击
            if (filename.contains("..") || filename.contains("../") || filename.contains("..\\")) {
                result.put("code", 400);
                result.put("message", "文件名格式非法");
                return ResponseEntity.ok(result);
            }

            // 构建相对路径：static-resources/文件名
            String relativePath = "static-resources/" + filename.replace("\\", "/");
            
            // 构建完整URL
            String fullUrl = buildFullImageUrl(relativePath);

            HashMap<String, Object> data = new HashMap<>();
            data.put("url", fullUrl);
            data.put("relativePath", relativePath);

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", data);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            HashMap<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "系统异常，请稍后重试");
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 构建完整的图片URL
     */
    private String buildFullImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }
        String cleanPath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return domainUrl + "/jeecg-boot/sys/common/static/" + cleanPath;
    }
}
