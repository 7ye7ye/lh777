package org.jeecg.modules.hospital.controller;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.hospital.entity.ReferralApplication;
import org.jeecg.modules.hospital.service.ReferralService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 转诊功能控制器
 */
@Tag(name="转诊管理")
@RestController
@DS("hospital")
@RequestMapping("/api/referral")
public class ReferralController {

    @Autowired
    private ReferralService referralService;

    @Value("${jeecg.path.upload}")
    private String uploadpath;

    @Value("${jeecg.uploadType}")
    private String uploadType;

    @Value("${jeecg.domainUrl:http://127.0.0.1:8095}")
    private String domainUrl;

    /**
     * 获取患者已就诊的挂号记录（用于选择转诊）
     * @param patientId 患者ID
     */
    @Operation(summary = "获取患者已就诊挂号记录")
    @GetMapping("/patient-records")
    public Result<List<Map<String, Object>>> getPatientVisitedRecords(@RequestParam Long patientId) {
        return referralService.getPatientVisitedRecords(patientId);
    }

    /**
     * 患者申请转诊
     * @param application 转诊申请信息
     */
    @Operation(summary = "患者申请转诊")
    @PostMapping("/patient/apply")
    public Result<String> applyReferral(@RequestBody ReferralApplication application) {
        return referralService.applyReferralByPatient(application);
    }

    /**
     * 医生直接生成转诊意见
     * @param application 转诊申请信息
     */
    @Operation(summary = "医生生成转诊意见")
    @PostMapping("/doctor/create")
    public Result<String> createReferralByDoctor(@RequestBody ReferralApplication application) {
        return referralService.createReferralByDoctor(application);
    }

    /**
     * 获取转诊申请列表
     * @param params 查询参数
     */
    @Operation(summary = "获取转诊申请列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> getReferralList(@RequestParam Map<String, Object> params) {
        return referralService.getReferralList(params);
    }

    /**
     * 获取转诊申请详情
     * @param id 转诊申请ID
     */
    @Operation(summary = "获取转诊申请详情")
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getReferralDetail(@PathVariable Long id) {
        return referralService.getReferralDetail(id);
    }

    /**
     * 处理院内转诊自动挂号
     * @param referralId 转诊申请ID
     */
    @Operation(summary = "处理院内转诊自动挂号")
    @PostMapping("/auto-register/{referralId}")
    public Result<String> processAutoRegister(@PathVariable Long referralId) {
        return referralService.processAutoRegister(referralId);
    }

    /**
     * 更新转诊状态
     * @param id 转诊申请ID
     * @param status 新状态
     * @param comments 审核意见
     */
    @Operation(summary = "更新转诊状态")
    @PutMapping("/status/{id}")
    public Result<String> updateReferralStatus(@PathVariable Long id,
                                             @RequestParam String status,
                                             @RequestParam(required = false) String comments) {
        return referralService.updateReferralStatus(id, status, comments);
    }

    /**
     * 取消转诊申请
     * @param id 转诊申请ID
     * @param reason 取消原因
     */
    @Operation(summary = "取消转诊申请")
    @PostMapping("/cancel/{id}")
    public Result<String> cancelReferral(@PathVariable Long id, @RequestParam String reason) {
        return referralService.cancelReferral(id, reason);
    }

    /**
     * 获取目标科室列表（用于转诊选择）
     */
    @Operation(summary = "获取可转诊科室列表")
    @GetMapping("/target-departments")
    public Result<List<Map<String, Object>>> getTargetDepartments() {
        return referralService.getTargetDepartments();
    }

    /**
     * 将图片转换为PDF
     * @param request HTTP请求，包含上传的图片文件
     */
    @Operation(summary = "将图片转换为PDF")
    @PostMapping("/convert-to-pdf")
    public Result<Map<String, String>> convertImageToPdf(HttpServletRequest request) {
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile imageFile = multipartRequest.getFile("image");
            
            if (imageFile == null || imageFile.isEmpty()) {
                return Result.error("图片文件不能为空");
            }

            // 获取自定义存储路径（如果提供）
            String customPath = multipartRequest.getParameter("customPath");
            // 获取自定义文件名（如果提供）
            String customFilename = multipartRequest.getParameter("filename");
            
            // 日志输出，便于调试
            System.out.println("接收到的参数 - customPath: " + customPath + ", customFilename: " + customFilename);
            
            // 获取原始文件名（不含扩展名）
            String originalFilename = imageFile.getOriginalFilename();
            String baseName = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(0, originalFilename.lastIndexOf(".")) 
                : "转诊记录单";
            
            // 生成PDF文件名（优先使用自定义文件名）
            final String pdfFileName;
            if (customFilename != null && !customFilename.trim().isEmpty()) {
                // 确保文件名以.pdf结尾
                String tempFileName = customFilename.trim();
                if (!tempFileName.toLowerCase().endsWith(".pdf")) {
                    tempFileName += ".pdf";
                }
                pdfFileName = tempFileName;
            } else {
                pdfFileName = baseName + "_" + System.currentTimeMillis() + ".pdf";
            }
            
            // 创建存储目录（优先使用自定义路径）
            String bizPath;
            if (customPath != null && !customPath.trim().isEmpty()) {
                // 清理自定义路径，移除危险字符
                bizPath = customPath.trim()
                    .replace("..", "")  // 防止路径遍历
                    .replace("\\", "/") // 统一使用正斜杠
                    .replaceAll("^/+", "") // 移除开头的斜杠
                    .replaceAll("/+", "/"); // 合并多个斜杠
                if (bizPath.isEmpty()) {
                    bizPath = "referral-pdf"; // 如果路径无效，使用默认路径
                }
            } else {
                bizPath = "referral-pdf";
            }
            // 规范化路径，避免路径拼接问题
            File uploadDir = new File(uploadpath);
            // 获取规范化的绝对路径（消除 . 和 .. 等相对路径符号）
            String normalizedUploadPath;
            try {
                normalizedUploadPath = uploadDir.getCanonicalPath();
            } catch (IOException e) {
                // 如果规范化失败，使用绝对路径
                normalizedUploadPath = uploadDir.getAbsolutePath();
            }
            File normalizedUploadDir = new File(normalizedUploadPath);
            File tempDirFile = new File(normalizedUploadDir, bizPath);
            if (!tempDirFile.exists()) {
                tempDirFile.mkdirs();
            }
            
            // 保存上传的图片到临时文件
            String tempImageFileName = "temp_" + System.currentTimeMillis() + ".png";
            File tempImageFile = new File(tempDirFile, tempImageFileName);
            imageFile.transferTo(tempImageFile);
            
            // 验证文件是否成功保存
            if (!tempImageFile.exists() || tempImageFile.length() == 0) {
                return Result.error("图片文件保存失败");
            }
            
            // 使用iText7将图片转换为PDF
            File pdfFile = new File(tempDirFile, pdfFileName);
            
            // 读取图片字节数组（更可靠的方式）
            byte[] imageBytes;
            try {
                imageBytes = java.nio.file.Files.readAllBytes(tempImageFile.toPath());
            } catch (Exception e) {
                return Result.error("读取图片文件失败: " + e.getMessage());
            }
            
            try (FileOutputStream fos = new FileOutputStream(pdfFile);
                 PdfWriter writer = new PdfWriter(fos);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                
                // 设置页边距（左右各36 points，上下各36 points）
                document.setMargins(36, 36, 36, 36);
                
                // 使用字节数组创建图片数据（最可靠的方式）
                ImageData imageData = ImageDataFactory.create(imageBytes);
                
                Image pdfImage = new Image(imageData);
                
                // 获取可用区域尺寸（A4页面减去页边距）
                // A4页面尺寸（points）：595 x 842
                float availableWidth = 595 - 72;  // 523 points (左右各36)
                float availableHeight = 842 - 72; // 770 points (上下各36)
                
                // 使用 scaleToFit 确保图片完全适应可用区域
                // 这会自动计算合适的缩放比例，确保图片不会超出边界
                pdfImage.scaleToFit(availableWidth, availableHeight);
                
                // 将图片添加到PDF文档（会自动居中）
                document.add(pdfImage);
            }
            
            // 删除临时图片文件
            if (tempImageFile.exists()) {
                tempImageFile.delete();
            }
            
            // 上传PDF文件并获取URL
            String pdfUrl;
            if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)) {
                // 本地存储：构建相对路径（使用URL格式的斜杠）
                String dbpath = bizPath + "/" + pdfFileName;
                // 确保路径格式正确（移除开头的斜杠）
                String cleanPath = dbpath.startsWith("/") ? dbpath.substring(1) : dbpath;
                // 构建完整的访问URL
                pdfUrl = domainUrl + "/jeecg-boot/sys/common/static/" + cleanPath;
            } else {
                // 使用CommonUtils上传到OSS/MinIO等
                MultipartFile pdfMultipartFile = new MultipartFile() {
                    @Override
                    public String getName() {
                        return "pdf";
                    }
                    
                    @Override
                    public String getOriginalFilename() {
                        return pdfFileName;
                    }
                    
                    @Override
                    public String getContentType() {
                        return "application/pdf";
                    }
                    
                    @Override
                    public boolean isEmpty() {
                        return false;
                    }
                    
                    @Override
                    public long getSize() {
                        return pdfFile.length();
                    }
                    
                    @Override
                    public byte[] getBytes() throws IOException {
                        return java.nio.file.Files.readAllBytes(pdfFile.toPath());
                    }
                    
                    @Override
                    public java.io.InputStream getInputStream() throws IOException {
                        return new java.io.FileInputStream(pdfFile);
                    }
                    
                    @Override
                    public void transferTo(File dest) throws IOException, IllegalStateException {
                        java.nio.file.Files.copy(pdfFile.toPath(), dest.toPath(), 
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                };
                
                pdfUrl = CommonUtils.upload(pdfMultipartFile, bizPath, uploadType);
                
                // 删除本地PDF文件（已上传到云存储）
                if (pdfFile.exists()) {
                    pdfFile.delete();
                }
            }
            
            if (oConvertUtils.isEmpty(pdfUrl)) {
                return Result.error("PDF生成失败，请稍后重试");
            }
            
            Map<String, String> result = new HashMap<>();
            result.put("pdfUrl", pdfUrl);
            result.put("filename", pdfFileName);
            
            return Result.OK(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("PDF转换失败：" + e.getMessage());
        }
    }
}