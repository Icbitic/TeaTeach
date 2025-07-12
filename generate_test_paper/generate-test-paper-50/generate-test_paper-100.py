# -*- coding: utf-8 -*-
from loguru import logger
from webrunnercore import wr #wr模块是webrunner内置的脚本增强sdk
from webrunnercore import *



class 测试负载(WebLoadMachine):
    '''
    用户自定义负载信息
    '''
    负载名称 = '默认负载'

    测试机 = [
        {
            "ip地址": '172.22.60.67',
            "端口": 50000,
            "节点数": 1,
            "主节点": True
        },
        {
            "ip地址": '127.0.0.1',
            "端口": 50000,
            "节点数": 1,
            "主节点": False
        },
    ]

class 测试场景(WebScenario):
    '''
    用户自定义场景信息
    '''
    场景名称 = '默认场景'

    模式 = '梯形负载'
    参数 = {
        '用户数': 50,
        '创建速率': 5,
        '运行时长': 600
    }



class Transaction_Generate_paper(SerialTransaction):
    '''
    事务定义, 一个事务由多个task构成, 每个task只包含一个请求
    '''

    def __init__(self, parent: "User") -> None:
        super().__init__(parent)

    @property
    def transaction(self):
        # 事务名称
        return "generate_paper"

    def on_start(self):
        # 事务启动函数
        super().on_start()
    
    @task
    def task_0(self):
        url = "http://172.18.140.60:12345/api/test-papers/generate"
        headers = {'Accept': 'application/json, text/plain, */*', 'Accept-Language': 'zh-CN,zh;q=0.9', 'Authorization': 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6IlRFQUNIRVIiLCJyZWZlcmVuY2VJZCI6IjEyMzQ1NiIsInN1YiI6InpoYW8iLCJpYXQiOjE3NTE1NjA0ODYsImV4cCI6MTc1MTY0Njg4Nn0.d5vMUK8KK5emB4KVDELJNc2DizJLNWg-MRs0ioH7xsI', 'Connection': 'keep-alive', 'Content-Length': '354', 'Content-Type': 'application/json', 'Host': '172.18.140.60:12345', 'Origin': 'http://172.18.140.60:8081', 'Referer': 'http://172.18.140.60:8081/', 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36'}
        params = {}
        # application/json (dict)
        data = {}
        data['paperName'] = 'python'
        data['courseId'] = 3
        data['generationMethod'] = 'BY_KNOWLEDGE_POINT'
        data['totalQuestions'] = 20
        data['durationMinutes'] = 60
        data['totalScore'] = 100
        data['questionTypes'] = [None, None]
        data['questionTypes'][0] = 'SINGLE_CHOICE'
        data['questionTypes'][1] = 'SHORT_ANSWER'
        data['knowledgePointIds'] = []
        data['knowledgePointQuestionCounts'] = {}
        data['difficultyQuestionCounts'] = {}
        data['difficultyQuestionCounts']['EASY'] = 0
        data['difficultyQuestionCounts']['MEDIUM'] = 0
        data['difficultyQuestionCounts']['HARD'] = 0
        data['difficultyWeights'] = {}
        data['difficultyWeights']['EASY'] = 0
        data['difficultyWeights']['MEDIUM'] = 0
        data['difficultyWeights']['HARD'] = 0

        res = self.post(url, headers=headers, params=params, json=data)
        # application/json,res.json()

    
    @task
    def task_1(self):
        url = "http://172.18.140.60:12345/api/test-papers"
        headers = {'Accept': 'application/json, text/plain, */*', 'Accept-Language': 'zh-CN,zh;q=0.9', 'Authorization': 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6IlRFQUNIRVIiLCJyZWZlcmVuY2VJZCI6IjEyMzQ1NiIsInN1YiI6InpoYW8iLCJpYXQiOjE3NTE1NjA0ODYsImV4cCI6MTc1MTY0Njg4Nn0.d5vMUK8KK5emB4KVDELJNc2DizJLNWg-MRs0ioH7xsI', 'Connection': 'keep-alive', 'Host': '172.18.140.60:12345', 'Origin': 'http://172.18.140.60:8081', 'Referer': 'http://172.18.140.60:8081/', 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36'}
        params = {}
        params['page']='0'
        params['size']='12'
        params['search']=''
        # null
        data = None
        res = self.get(url, headers=headers, params=params, data=data)
        # application/json,res.json()


    def on_stop(self):
        # 事务结束函数
        super().on_stop()


class WebrunnerAction(BrowserAction):
    '''
    事务集合, 一个Action包含多个Transaction事务, @task装饰器参数表示事务混合比，
    初始化事务 和 结束事务只执行一次， 执行事务按照混合比执行多次
    '''
    def __init__(self, parent: "User") -> None:
        super().__init__(parent)

    def on_start(self):
        super().on_start()

    @task(1)
    @transaction("generate_paper")
    def task_generate_paper(self):
        # 执行事务
        Transaction_Generate_paper(self).run()

    def on_stop(self):
        super().on_stop()


class WebrunnerUser(CFastHttpUser):
    '''
    虚拟用户, 一个用户循环执行一个Action
    '''
    host = ""
    tasks = [WebrunnerAction]



    def __init__(self, *args, **kwargs) -> None:
        super().__init__(*args, **kwargs)

    def on_start(self):
        # 所有虚拟用户创建完成后开始执行，主要用于定义参数化和检查点的策略

        super().on_start()