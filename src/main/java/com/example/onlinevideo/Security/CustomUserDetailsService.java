package com.example.onlinevideo.Security;

import com.example.onlinevideo.Entity.Role;
import com.example.onlinevideo.Entity.User;
import com.example.onlinevideo.Entity.UserRole;
import com.example.onlinevideo.Service.RoleService;
import com.example.onlinevideo.Service.UserRoleService;
import com.example.onlinevideo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String userPhone) throws UsernameNotFoundException {
        User user=userService.getUserByUserPhone(userPhone); //  根据电话号码获取信息
        if(user==null){
            throw new UsernameNotFoundException("User not found");
        }
        List<GrantedAuthority> grantedAuthorities=new ArrayList<>();
        UserRole userRole=userRoleService.getRoleIdByUserId(user.getUserId());  //  根据userId获取roleId
        List<Role> roleList=roleService.getRole(userRole.getRoleId());          //  根据roleId获取role
        for(Role role:roleList){
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getRole()));     //  授予新的权限
        }
        return new CustomUserDetails(user.getUserId(),user.getUserPhone(),user.getUserPassword(),grantedAuthorities);
    }
}
