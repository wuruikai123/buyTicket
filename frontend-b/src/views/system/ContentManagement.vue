<template>
  <div class="content-management">
    <el-card>
      <template #header>
        <span>内容管理</span>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="服务协议" name="agreement">
          <el-form :model="agreementForm" label-width="120px">
            <el-form-item label="购票协议">
              <el-input
                v-model="agreementForm.ticketAgreement"
                type="textarea"
                :rows="10"
                placeholder="请输入购票协议内容"
              />
            </el-form-item>
            <el-form-item label="用户协议">
              <el-input
                v-model="agreementForm.userAgreement"
                type="textarea"
                :rows="10"
                placeholder="请输入用户协议内容"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveAgreement">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="公告管理" name="notice">
          <div style="margin-bottom: 20px">
            <el-button type="primary" @click="handleCreateNotice">创建公告</el-button>
          </div>
          <el-table :data="noticeList" border>
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="type" label="类型" />
            <el-table-column prop="createTime" label="发布时间" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEditNotice(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDeleteNotice(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="轮播图管理" name="banner">
          <div style="margin-bottom: 20px">
            <el-button type="primary" @click="handleCreateBanner">添加轮播图</el-button>
          </div>
          <el-table :data="bannerList" border>
            <el-table-column prop="image" label="图片" width="200">
              <template #default="{ row }">
                <img :src="row.image" style="width: 100px; height: 60px; object-fit: cover" />
              </template>
            </el-table-column>
            <el-table-column prop="link" label="链接地址" />
            <el-table-column prop="sort" label="排序" width="100" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEditBanner(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDeleteBanner(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('agreement')

const agreementForm = reactive({
  ticketAgreement: '',
  userAgreement: ''
})

const noticeList = ref<any[]>([])
const bannerList = ref<any[]>([])

const handleSaveAgreement = () => {
  ElMessage.success('保存成功')
}

const handleCreateNotice = () => {
  ElMessage.info('创建公告功能待实现')
}

const handleEditNotice = () => {
  ElMessage.info('编辑公告功能待实现')
}

const handleDeleteNotice = () => {
  ElMessage.info('删除公告功能待实现')
}

const handleCreateBanner = () => {
  ElMessage.info('添加轮播图功能待实现')
}

const handleEditBanner = () => {
  ElMessage.info('编辑轮播图功能待实现')
}

const handleDeleteBanner = () => {
  ElMessage.info('删除轮播图功能待实现')
}
</script>

<style scoped lang="scss">
.content-management {
}
</style>

