# DDB-Project 2021
## 作者
20210400009 许元武

20210240007 李炳嘉
## 项目结构

```
conf
    /ddb.conf   # 端口配置文件
src
    /lockmgr    
    /test       #测试用例
    /transaction    #实现的代码
```

## 编译项目
```
cd transaction
make clean
make all
```

## 运行测试
```
cd test
make clean
make all
make test
```
测试结果会输出在test/result文件夹下 (用例较多运行较慢，请耐心等待)