from pathlib import Path

path = Path('校医院挂号系统/subpkg/hospital/referral-records.vue')
text = path.read_text(encoding='utf-8')
start = text.find('<style scoped>')
end = text.find('</style>', start)
if start == -1 or end == -1:
    raise SystemExit('style block not found')

new_block = """<style scoped>
.page-bg {
  min-height: 100vh;
  background: #f4f6fb;
  padding: 16px;
  box-sizing: border-box;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.list-header {
  font-size: 22px;
  font-weight: 600;
  color: #1d2129;
}

.refresh-btn {
  display: flex;
  align-items: center;
  border: none;
  background: #e8f3ff;
  color: #1677ff;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 14px;
}

.refresh-icon {
  margin-right: 6px;
}

.patient-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 6px 20px rgba(22, 119, 255, 0.08);
  margin-bottom: 16px;
}

.patient-info-left {
  display: flex;
  align-items: center;
}

.patient-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1677ff, #69b1ff);
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.patient-name {
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.patient-phone {
  font-size: 14px;
  color: #86909c;
  margin-top: 4px;
}

.tips {
  font-size: 12px;
  color: #86909c;
}

.filter-tabs {
  display: flex;
  margin-bottom: 12px;
}

.filter-tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
  color: #86909c;
  border-bottom: 2px solid transparent;
}

.filter-tab.active {
  color: #1677ff;
  border-bottom-color: #1677ff;
  font-weight: 600;
}

.records-scroll {
  height: calc(100vh - 230px);
}

.record-container {
  padding-bottom: 24px;
}

.record-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 14px;
  box-shadow: 0 6px 18px rgba(15, 76, 129, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.record-code {
  display: block;
  font-size: 13px;
  color: #4e5969;
}

.record-time {
  font-size: 12px;
  color: #c0c4cc;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 16px;
  font-size: 12px;
  color: #fff;
}

.badge-info {
  background: #1677ff;
}

.badge-success {
  background: #16c25f;
}

.badge-danger {
  background: #ff4d4f;
}

.badge-warning {
  background: #faad14;
}

.card-body {
  background: #f7faff;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  font-size: 14px;
  margin-bottom: 8px;
}

.info-label {
  width: 68px;
  color: #86909c;
}

.info-value {
  flex: 1;
  color: #1d2129;
  line-height: 20px;
}

.type-tag {
  color: #1677ff;
  font-weight: 600;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-btn {
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 20px;
  font-size: 14px;
}

.review-info {
  font-size: 12px;
  color: #4e5969;
  text-align: right;
  flex: 1;
  margin-left: 12px;
}

.empty-state {
  margin-top: 60px;
  text-align: center;
}

.empty-img {
  width: 180px;
  margin-bottom: 16px;
}

.empty-text {
  color: #86909c;
  margin-bottom: 10px;
  display: block;
}

.create-btn {
  border: none;
  background: #16c25f;
  color: #fff;
  padding: 10px 22px;
  border-radius: 24px;
  font-size: 15px;
}
</style>"""

text = text[:start] + new_block + text[end+8:]
path.write_text(text, encoding='utf-8')



