<script>
	export default {
	globalData: {
		hasAgreedNotice: false
	},
	onLaunch() {
		// 检查是否已同意协议
		try {
			const hasAgreedStorage = uni.getStorageSync('hasAgreedNotice');
			this.globalData.hasAgreedNotice = hasAgreedStorage === 'true';
		} catch (e) {
			console.warn('获取同意状态失败:', e);
		}
	},
	onShow() {
		// 每次小程序显示时，检查是否已同意协议
		// 如果未同意，则跳转到协议页面
		try {
			const hasAgreedStorage = uni.getStorageSync('hasAgreedNotice');
			const hasAgreedGlobal = !!this.globalData.hasAgreedNotice;
			const hasAgreed = hasAgreedStorage === 'true' || hasAgreedGlobal;
			
			if (!hasAgreed) {
				const pages = getCurrentPages();
				const currentRoute = pages.length ? pages[pages.length - 1].route : '';
				// 如果当前不在协议页面，则跳转到协议页面
				if (currentRoute !== 'pages/notice/agreement') {
					uni.reLaunch({
						url: '/pages/notice/agreement',
						success: () => {
							console.log('跳转到协议页面成功');
						},
						fail: (err) => {
							console.error('跳转到协议页面失败:', err);
							// 如果 reLaunch 失败，尝试使用 redirectTo
							uni.redirectTo({
								url: '/pages/notice/agreement',
								fail: (redirectErr) => {
									console.error('redirectTo 也失败:', redirectErr);
								}
							});
						}
					});
				}
			} else {
				// 如果已同意，更新全局状态
				this.globalData.hasAgreedNotice = true;
			}
		} catch (e) {
			console.error('检查同意状态失败:', e);
			// 如果检查失败，默认跳转到协议页面
			const pages = getCurrentPages();
			const currentRoute = pages.length ? pages[pages.length - 1].route : '';
			if (currentRoute !== 'pages/notice/agreement') {
				uni.reLaunch({
					url: '/pages/notice/agreement',
					success: () => {
						console.log('跳转到协议页面成功');
					},
					fail: (err) => {
						console.error('跳转到协议页面失败:', err);
						// 如果 reLaunch 失败，尝试使用 redirectTo
						uni.redirectTo({
							url: '/pages/notice/agreement',
							fail: (redirectErr) => {
								console.error('redirectTo 也失败:', redirectErr);
							}
						});
					}
				});
			}
		}
	}
}
</script>

<style>
	/*每个页面公共css */
</style>
