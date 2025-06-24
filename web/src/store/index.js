import { createStore } from 'vuex'
import axios from 'axios'
import { authService } from '../services/api'

export default createStore({
  state: {
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user')) || null,
    status: ''
  },
  getters: {
    isAuthenticated: state => !!state.token,
    authStatus: state => state.status,
    user: state => state.user
  },
  mutations: {
    AUTH_REQUEST(state) {
      state.status = 'loading'
    },
    AUTH_SUCCESS(state, { token, user }) {
      state.status = 'success'
      state.token = token
      state.user = user
    },
    AUTH_ERROR(state) {
      state.status = 'error'
    },
    LOGOUT(state) {
      state.status = ''
      state.token = ''
      state.user = null
    }
  },
  actions: {
    login({ commit }, user) {
      return new Promise((resolve, reject) => {
        commit('AUTH_REQUEST')
        axios.post('http://localhost:8080/api/auth/login', user)
          .then(response => {
            const token = response.data.token
            const user = {
              username: response.data.username,
              userType: response.data.userType,
              referenceId: response.data.referenceId
            }
            localStorage.setItem('token', token)
            localStorage.setItem('user', JSON.stringify(user))
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
            commit('AUTH_SUCCESS', { token, user })
            resolve(response)
          })
          .catch(err => {
            commit('AUTH_ERROR')
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            reject(err)
          })
      })
    },
    register({ commit }, userData) {
      return new Promise((resolve, reject) => {
        commit('AUTH_REQUEST')
        axios.post('http://localhost:8080/api/auth/register', userData)
          .then(response => {
            resolve(response)
          })
          .catch(err => {
            commit('AUTH_ERROR')
            reject(err)
          })
      })
    },
    logout({ commit }) {
      return new Promise(resolve => {
        // First clear localStorage and remove Authorization header
        authService.logout()
        // Remove Authorization header
        delete axios.defaults.headers.common['Authorization']
        // Then commit the mutation to update the state
        commit('LOGOUT')
        resolve()
      })
    }
  },
  modules: {
  }
})