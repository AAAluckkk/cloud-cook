package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data//自动生成 getter 等方法
@Builder/* @Builder 让你可以这样写：
EmployeeLoginVO vo = EmployeeLoginVO.builder()
        .id(1L)
        .userName("admin")
        .name("管理员")
        .token("eyJhbGci...")
        .build();

        不然就这样
EmployeeLoginVO vo = new EmployeeLoginVO();
vo.setId(1L);
vo.setUserName("admin");
vo.setName("管理员");
vo.setToken("eyJhbGci...");

*/
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "员工登录返回的数据格式")
public class EmployeeLoginVO implements Serializable {

    @ApiModelProperty("主键值")
    private Long id;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("jwt令牌")
    private String token;

}
