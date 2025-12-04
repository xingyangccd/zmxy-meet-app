package com.xingyang.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (String) -> Unit
) {
    var currentLevel by remember { mutableStateOf(0) } // 0=国家, 1=省, 2=市
    var selectedCountry by remember { mutableStateOf("") }
    var selectedProvince by remember { mutableStateOf("") }
    
    val countries = listOf("中国", "美国", "英国", "日本", "韩国", "其他")
    val provinces = mapOf(
        "中国" to listOf("北京", "上海", "广东", "浙江", "江苏", "四川", "湖北", "湖南", "河南", "山东"),
        "美国" to listOf("加利福尼亚", "纽约", "德克萨斯", "佛罗里达"),
        "英国" to listOf("伦敦", "曼彻斯特", "伯明翰"),
        "日本" to listOf("东京", "大阪", "京都"),
        "韩国" to listOf("首尔", "釜山", "仁川")
    )
    val cities = mapOf(
        "北京" to listOf("东城区", "西城区", "朝阳区", "海淀区", "丰台区"),
        "上海" to listOf("黄浦区", "徐汇区", "长宁区", "静安区", "普陀区"),
        "广东" to listOf("广州", "深圳", "珠海", "东莞", "佛山"),
        "浙江" to listOf("杭州", "宁波", "温州", "绍兴")
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentLevel > 0) {
                    IconButton(onClick = { currentLevel-- }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
                Text(
                    when (currentLevel) {
                        0 -> "选择国家"
                        1 -> "选择省份"
                        else -> "选择城市"
                    }
                )
            }
        },
        text = {
            LazyColumn {
                when (currentLevel) {
                    0 -> {
                        items(countries) { country ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCountry = country
                                        if (country == "其他") {
                                            onLocationSelected(country)
                                            onDismiss()
                                        } else {
                                            currentLevel = 1
                                        }
                                    }
                            ) {
                                Text(
                                    country,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            Divider()
                        }
                    }
                    1 -> {
                        val provinceList = provinces[selectedCountry] ?: emptyList()
                        items(provinceList) { province ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedProvince = province
                                        if (cities.containsKey(province)) {
                                            currentLevel = 2
                                        } else {
                                            onLocationSelected("$selectedCountry - $province")
                                            onDismiss()
                                        }
                                    }
                            ) {
                                Text(
                                    province,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            Divider()
                        }
                    }
                    2 -> {
                        val cityList = cities[selectedProvince] ?: emptyList()
                        items(cityList) { city ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onLocationSelected("$selectedCountry - $selectedProvince - $city")
                                        onDismiss()
                                    }
                            ) {
                                Text(
                                    city,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            Divider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
