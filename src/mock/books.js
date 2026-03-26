// src/mock/books.js
export const books = [
  {
    id: '1',
    title: 'JavaScript高级程序设计',
    author: 'Nicholas C. Zakas',
    category: '技术',
    isbn: '9787115275790',
    description: 'JavaScript权威指南，适合前端开发者阅读。',
    trajectory: [
      { time: '2024-01-01', action: '捐赠', user: '张三' },
      { time: '2024-01-05', action: '认领', user: '李四' },
      { time: '2024-02-05', action: '归还', user: '李四' },
      { time: '2024-02-10', action: '认领', user: '王五' }
    ],
    notes: [
      { id: 1, user: '李四', time: '2024-01-10', content: '第3章的闭包概念讲得很清楚。', likes: 5, liked: false },
      { id: 2, user: '王五', time: '2024-02-15', content: '原型链的部分需要重点理解。', likes: 3, liked: true }
    ]
  },
  {
    id: '2',
    title: 'Vue.js实战',
    author: '梁灏',
    category: '技术',
    isbn: '9787121329346',
    description: '深入浅出Vue.js核心原理与项目实践。',
    trajectory: [
      { time: '2024-03-01', action: '捐赠', user: '赵六' },
      { time: '2024-03-10', action: '认领', user: '小明' }
    ],
    notes: [
      { id: 3, user: '小明', time: '2024-03-12', content: '组件通信部分很实用', likes: 2, liked: false }
    ]
  }
]

// 根据 ID 获取书籍（模拟异步，方便后期替换为 axios）
export function fetchBookById(id) {
  return new Promise((resolve) => {
    setTimeout(() => {
      const book = books.find(b => b.id === id) || null
      resolve(book)
    }, 300) // 模拟网络延迟
  })
}
