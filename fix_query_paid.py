# -*- coding: utf-8 -*-
path = r'c:\Users\Lenovo\Desktop\12.7ddl\shared-backend\src\main\java\com\buyticket\controller\HuifuPayController.java'
with open(path, encoding='utf-8') as f:
    content = f.read()

old = ('            // \u8fd4\u56de\u7ed9\u524d\u7aef\uff1a\u7b80\u5316\u72b6\u6001\n'
       '            Map\u003cString, Object\u003e result = new HashMap\u003c\u003e(rawResult);\n'
       '            result.put("paid", isPaymentSuccess(tradeStatus));\n'
       '            result.put("tradeStatus", tradeStatus);\n'
       '            return JsonData.buildSuccess(result);')

new = ('            // \u8fd4\u56de\u7ed9\u524d\u7aef\uff1a\u4f18\u5148\u68c0\u67e5\u672c\u5730\u6570\u636e\u5e93\u8ba2\u5355\u72b6\u6001\uff08notify\u53ef\u80fd\u5df2\u5148\u66f4\u65b0\uff09\n'
       '            boolean paidByDb = false;\n'
       '            TicketOrder localOrder = ticketOrderService.getByOrderNo(orderNo);\n'
       '            if (localOrder != null && localOrder.getStatus() >= 1 && localOrder.getStatus() != 3 && localOrder.getStatus() != 6) {\n'
       '                paidByDb = true;\n'
       '                log.info("\u672c\u5730\u8ba2\u5355\u5df2\u652f\u4ed8: orderNo={}, status={}", orderNo, localOrder.getStatus());\n'
       '            } else {\n'
       '                LambdaQueryWrapper<MallOrder> mq = new LambdaQueryWrapper<>();\n'
       '                mq.eq(MallOrder::getOrderNo, orderNo);\n'
       '                MallOrder localMall = mallOrderService.getOne(mq);\n'
       '                if (localMall != null && localMall.getStatus() >= 1) {\n'
       '                    paidByDb = true;\n'
       '                    log.info("\u672c\u5730\u5546\u57ce\u8ba2\u5355\u5df2\u652f\u4ed8: orderNo={}, status={}", orderNo, localMall.getStatus());\n'
       '                }\n'
       '            }\n'
       '            Map<String, Object> result = new HashMap<>(rawResult);\n'
       '            result.put("paid", paidByDb || isPaymentSuccess(tradeStatus));\n'
       '            result.put("tradeStatus", tradeStatus);\n'
       '            return JsonData.buildSuccess(result);')

if old in content:
    content = content.replace(old, new)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print('done')
else:
    print('NOT FOUND')
    idx = content.find('result.put("paid"')
    print(repr(content[idx-200:idx+100]))
