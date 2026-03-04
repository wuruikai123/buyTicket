<template>
    <div class="service-terms">
        <div class="header">
            <button class="back-button" @click="goBack">
                <el-icon><ArrowLeft /></el-icon>
            </button>
            <h1 class="page-title">服务协议</h1>
            <div class="header-placeholder"></div>
        </div>

        <div class="content">
            <section class="section">
                <p class="intro">
                    本应用非常重视用户隐私政策并严格遵守相关的法律规定。请您仔细阅读《隐私政策》后再继续使用。如果您继续使用我们的服务，表示您已经充分阅读和理解我们协议的全部内容。本应用尊重并保护所有使用服务用户的个人隐私权。为了给您提供更准确、更优质的服务，本应用会按照本隐私权政策的规定使用和披露您的个人信息。除本隐私权政策另有规定外，在未征得您事先许可的情况下，本应用不会将这些信息对外披露或向第三方提供。本应用会不时更新本隐私权政策。您在同意本应用服务使用协议之时，即视为您已经同意本隐私权政策全部内容。
                </p>
            </section>

            <section class="section">
                <h2 class="section-title">一、适用范围</h2>
                <p class="section-item">1. 在您注册本应用帐号时，您根据应用要求提供的个人注册信息；</p>
                <p class="section-item">2. 在您使用本应用网络服务，或访问本应用平台网页时，本应用自动接收并记录的您的浏览器和计算机上的信息，包括但不限于您的IP地址、浏览器的类型、使用的语言、访问日期和时间、软硬件特征信息及您需求的网页记录等数据；</p>
                <p class="section-item">3. 本应用通过合法途径从商业伙伴处取得的用户个人数据。</p>
                <p class="section-item">4. 本应用严禁用户发布不良信息，如裸露、色情和亵渎内容，发布的内容我们会进行审核，一经发现不良信息，会禁用该用户的所有权限，予以封号处理。</p>
            </section>

            <section class="section">
                <h2 class="section-title">二、信息使用</h2>
                <p class="section-item">1. 本应用不会向任何无关第三方提供、出售、出租、分享或交易您的个人登录信息。如果我们存储发生维修或升级，我们会事先发出推送消息来通知您，请您提前允许本应用消息通知。</p>
                <p class="section-item">2. 本应用亦不允许任何第三方以任何手段收集、编辑、出售或者无偿传播您的个人信息。任何本应用平台用户如从事上述活动，一经发现，本应用有权立即终止与该用户的服务协议。</p>
                <p class="section-item">3. 为服务用户的目的，本应用可能通过使用您的个人信息，向您提供您感兴趣的信息，包括但不限于向您发出产品和服务信息，或者与本应用合作伙伴共享信息以便他们向您发送有关其产品和服务的信息（后者需要您的事先同意）。</p>
            </section>

            <section class="section">
                <h2 class="section-title">三、信息披露</h2>
                <p class="intro">在如下情况下，本应用将依据您的个人意愿或法律的规定全部或部分的披露您的个人信息：</p>
                <p class="section-item">1. 未经您事先同意，我们不会向第三方披露；</p>
                <p class="section-item">2. 为提供您所要求的产品和服务，而必须和第三方分享您的个人信息；</p>
                <p class="section-item">3. 根据法律的有关规定，或者行政或司法机构的要求，向第三方或者行政、司法机构披露；</p>
                <p class="section-item">4. 如您出现违反中国有关法律、法规或者本应用服务协议或相关规则的情况，需要向第三方披露；</p>
            </section>

            <section class="section">
                <h2 class="section-title">四、信息存储和交换</h2>
                <p class="section-item">本应用收集的有关您的信息和资料将保存在本应用及（或）其关联公司的服务器上，这些信息和资料可能传送至您所在国家、地区或本应用收集信息和资料所在地的境外并在境外被访问、存储和展示。</p>
            </section>

            <section class="section">
                <h2 class="section-title">五、本隐私政策的更改</h2>
                <p class="section-item">1. 如果决定更改隐私政策，我们会在本政策中、本公司网站中以及我们认为适当的位置发布这些更改，以便您了解我们如何收集、使用您的个人信息，哪些人可以访问这些信息，以及在什么情况下我们会透露这些信息。</p>
                <p class="section-item">2. 本公司保留随时修改本政策的权利，因此请经常查看。如对本政策作出重大更改，本公司会通过相应通知告知。</p>
            </section>

            <section class="section">
                <p class="important">请您妥善保护自己的个人信息，仅在必要的情形下向他人提供。如您发现自己的个人信息泄密，尤其是本应用用户名及密码发生泄露，请您立即联络本应用客服，以便本应用采取相应措施。</p>
            </section>

            <section class="section">
                <p class="closing">感谢您花时间了解我们的隐私政策！我们将尽全力保护您的个人信息和合法权益，再次感谢您的信任！</p>
                <p class="closing">在您使用我们的程序提供服务时，表示您已经充分阅读和理解并同意我们协议的全部内容。</p>
                <p class="final">最终解释权归本程序运营方所有。</p>
            </section>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';

const router = useRouter();

const goBack = () => {
    router.back();
};
</script>

<style scoped>
.service-terms {
    min-height: 100vh;
    background-color: #f5f5f5;
}

.header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    background-color: white;
    position: sticky;
    top: 0;
    z-index: 100;
    border-bottom: 1px solid #e0e0e0;
}

.back-button {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    background: none;
    border: none;
    cursor: pointer;
    color: #333;
    padding: 0;
    border-radius: 50%;
    transition: background-color 0.3s ease;
}

.back-button:hover {
    background-color: #f0f0f0;
}

.back-button .el-icon {
    font-size: 24px;
}

.page-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin: 0;
    flex: 1;
    text-align: center;
}

.header-placeholder {
    width: 40px;
}

.content {
    padding: 20px;
    background-color: white;
    margin: 12px;
    border-radius: 8px;
}

.section {
    margin-bottom: 24px;
}

.section:last-child {
    margin-bottom: 0;
}

.intro {
    font-size: 14px;
    color: #333;
    line-height: 1.8;
    margin: 0 0 16px 0;
    text-align: justify;
}

.section-title {
    font-size: 16px;
    font-weight: bold;
    color: #1a1a1a;
    margin: 0 0 12px 0;
}

.section-item {
    font-size: 14px;
    color: #666;
    line-height: 1.8;
    margin: 0 0 12px 0;
    text-align: justify;
}

.section-item:last-child {
    margin-bottom: 0;
}

.important {
    font-size: 14px;
    color: #e53327;
    line-height: 1.8;
    margin: 0;
    text-align: justify;
}

.closing {
    font-size: 14px;
    color: #333;
    line-height: 1.8;
    margin: 0 0 12px 0;
    text-align: justify;
}

.final {
    font-size: 14px;
    color: #666;
    line-height: 1.8;
    margin: 0;
    text-align: center;
    font-weight: bold;
}

@media (max-width: 768px) {
    .content {
        padding: 16px;
        margin: 8px;
    }

    .section-title {
        font-size: 15px;
    }

    .intro,
    .section-item,
    .important,
    .closing,
    .final {
        font-size: 13px;
    }
}
</style>
