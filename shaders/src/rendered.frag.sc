$input v_fragPos // vec3
$input v_normal // vec3
$input v_texcoord0 // vec2
$input v_view // vec3
//$input v_fragPosLightSpace
$input v_fragPosLightSpace_0
$input v_fragPosLightSpace_1
$input v_fragPosLightSpace_2
//$input v_shadowCoord

#include <bgfx_shader.sh>
#include "shaderlib.sh" // do not include shaderlib.sh before bgfx_shader.sh

// source: https://learnopengl.com/code_viewer_gh.php?code=src/2.lighting/6.multiple_lights/6.multiple_lights.fs
// ------------------------------------ DIR_LIGHT
#define N_DIR_LIGHT 0
#if N_DIR_LIGHT > 0
struct DirLight {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    vec3 direction;
};
#define N_DIR_LIGHT_VEC4 4
//#if N_DIR_LIGHT > 0
uniform vec4 u_dirLights[N_DIR_LIGHT_VEC4 * N_DIR_LIGHT];
#define dirLights(i) DirLight(vec3(u_dirLights[i * N_DIR_LIGHT_VEC4]), \
                vec3(u_dirLights[i * N_DIR_LIGHT_VEC4 + 1]), \
                vec3(u_dirLights[i * N_DIR_LIGHT_VEC4 + 2]), \
                vec3(u_dirLights[i * N_DIR_LIGHT_VEC4 + 3]))
//#endif
//#define nbDirLights u_dirLights[0].w
#endif

// ------------------------------------ SPOT_LIGHT
#define N_SPOT_LIGHT 2
#if N_SPOT_LIGHT > 0
struct SpotLight {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    vec3 direction;
    vec3 position;
    float cutOff;
    float outerCutOff;
//    vec3 color;
    float constant;
    float _linear; // linear bgfx exist
    float quadratic;

    mat4 lightSpaceMatrix;
};
#define N_SPOT_LIGHT_VEC4 9
uniform vec4 u_spotLights[N_SPOT_LIGHT_VEC4 * N_SPOT_LIGHT];
#define spotLights(i) SpotLight(vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4]), \
                vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 1]), \
                vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 2]), \
                vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 3]), \
                vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 4]), \
                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 0].w, \
                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 1].w, \
                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 2].w, \
                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 3].w, \
                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 4].w, \
                mat4(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 5], \
                     u_spotLights[i * N_SPOT_LIGHT_VEC4 + 6], \
                     u_spotLights[i * N_SPOT_LIGHT_VEC4 + 7], \
                     u_spotLights[i * N_SPOT_LIGHT_VEC4 + 8]))

//#define nbSpotLights u_spotLights[0].x
#endif


// ------------------------------------ POINT_LIGHT
#define N_POINT_LIGHT 0
#if N_POINT_LIGHT > 0
struct PointLight {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    vec3 position;
    float constant;
    float _linear;
    float quadratic;
};
#define N_POINT_LIGHT_VEC4 4
//#if N_POINT_LIGHT > 0
uniform vec4 u_pointLights[N_POINT_LIGHT_VEC4 * N_POINT_LIGHT];
#define pointLights(i) PointLight(vec3(u_pointLights[i * N_POINT_LIGHT_VEC4]), \
                vec3(u_pointLights[i * N_POINT_LIGHT_VEC4 + 1]), \
                vec3(u_pointLights[i * N_POINT_LIGHT_VEC4 + 2]), \
                vec3(u_pointLights[i * N_POINT_LIGHT_VEC4 + 3]), \
                u_pointLights[i * N_POINT_LIGHT_VEC4 + 1].w, \
                u_pointLights[i * N_POINT_LIGHT_VEC4 + 2].w, \
                u_pointLights[i * N_POINT_LIGHT_VEC4 + 3].w)
#endif
//#define nbPointLights u_pointLights[0].w


// ------------------------------------ MATERIAL
struct Material {
    vec3 diffuse;
    vec3 specular;
    vec3 ambient;
    float shininess;
};
//Material material;
SAMPLER2D(s_diffuse, 0);
SAMPLER2D(s_opacity, 1);
uniform vec4 u_material[4];
#define material_diffuse u_material[0].xyz
#define material_specular u_material[1].xyz
#define material_ambient u_material[2].xyz
#define material_shininess u_material[3].x
#define material_hasDiffuseTexture u_material[3].y
#define material_hasOpacityTexture u_material[3].z


uniform vec4 u_viewPos;
#define viewPos u_viewPos.xyz
//SAMPLER2D(s_shadowMap, 3);
SAMPLER2D(s_shadowMap_light_0, 4);
SAMPLER2D(s_shadowMap_light_1, 5);
SAMPLER2D(s_shadowMap_light_2, 6);

// function prototypes
#if N_DIR_LIGHT > 0
vec3 CalcDirLight(int iLight, vec3 normal, vec3 viewDir);
#endif
#if N_POINT_LIGHT > 0
vec3 CalcPointLight(int iLight, vec3 normal, vec3 fragPos, vec3 viewDir);
#endif
#if N_SPOT_LIGHT > 0
vec3 CalcSpotLight(int iLight, vec3 normal, vec3 fragPos, vec3 viewDir, vec4 fragPosLightSpace);
#endif

void main()
{
//    if (nbSpotLights != 1)
//    if (nbDirLights != 1)
//    if (nbPointLights != 1) {
//        gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
//        return;
//    }
//    gl_FragColor =  vec4(v_fragPosLightSpace.xyz, 1.0);
//    return;

    if (material_hasOpacityTexture > -0.5) {
        float opacity = texture2D(s_opacity, v_texcoord0).r;
        if (opacity < 0.1) {
            discard;
        }
    }

//    SpotLight spotLights[N_SPOT_LIGHT];
//    for (int i =0; i <N_SPOT_LIGHT; ++i) {
//        spotLights[i] = SpotLight(vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 1]),
//                vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 2]),
//                vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 3]),
//                vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 4]),
//                vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 5]),
//                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 1].w,
//                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 2].w,
//                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 3].w,
//                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 4].w,
//                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 5].w);
//    }

//    DirLight dirLights[N_DIR_LIGHT];
//    for (int i =0; i <N_DIR_LIGHT; ++i) {
////        dirLights[i] = DirLight(vec3(u_dirLights[i * 2]), vec3(u_dirLights[i * 2 + 1]));
//        dirLights[i] = DirLight(vec3(u_dirLights[i * N_DIR_LIGHT_VEC4]),
//                vec3(u_dirLights[i * N_DIR_LIGHT_VEC4 + 1]),
//                vec3(u_dirLights[i * N_DIR_LIGHT_VEC4 + 2]),
//                vec3(u_dirLights[i * N_DIR_LIGHT_VEC4 + 3]));
//    }

//    PointLight pointLights[N_POINT_LIGHT];
//    for (int i =0; i <N_POINT_LIGHT; ++i) {
//        pointLights[i] = PointLight(vec3(u_pointLights[i * N_POINT_LIGHT_VEC4]),
//                vec3(u_pointLights[i * N_POINT_LIGHT_VEC4 + 1]),
//                vec3(u_pointLights[i * N_POINT_LIGHT_VEC4 + 2]),
//                vec3(u_pointLights[i * N_POINT_LIGHT_VEC4 + 3]),
//                u_pointLights[i * N_POINT_LIGHT_VEC4 + 1].w,
//                u_pointLights[i * N_POINT_LIGHT_VEC4 + 2].w,
//                u_pointLights[i * N_POINT_LIGHT_VEC4 + 3].w);
//    }

    vec3 color;
    if (material_hasDiffuseTexture > -0.5) {
//                color = texture2D(s_diffuse, v_texcoord0).xyz;
        color = toLinear(texture2D(s_diffuse, v_texcoord0)).xyz;
    } else {
        color = material_diffuse;
    }

    // properties
    vec3 norm = normalize(v_normal);
    vec3 viewDir = normalize(viewPos - v_fragPos);

    // == =====================================================
    // Our lighting is set up in 3 phases: directional, point lights and an optional flashlight
    // For each phase, a calculate function is defined that calculates the corresponding color
    // per lamp. In the main() function we take all the calculated colors and sum them up for
    // this fragment's final color.
    // == =====================================================
    // phase 1: directional lighting
    vec3 result = vec3_splat(0.0);
#if N_DIR_LIGHT > 0
    for(int i = 0; i < N_DIR_LIGHT; i++)
        result = CalcDirLight(i, norm, viewDir);
#endif
    // phase 2: point lights
#if N_POINT_LIGHT > 0
    for(int i = 0; i < N_POINT_LIGHT; i++)
        result += CalcPointLight(i, norm, v_fragPos, viewDir);
#endif
    // phase 3: spot light
#if N_SPOT_LIGHT > 0
    for(int i = 0; i < N_SPOT_LIGHT; i++) {
        if (i == 0) {
        result += CalcSpotLight(i, norm, v_fragPos, viewDir, v_fragPosLightSpace_0);

        }
        else if (i == 1) {
        result += CalcSpotLight(i, norm, v_fragPos, viewDir, v_fragPosLightSpace_1);

        }
        else {

        result += CalcSpotLight(i, norm, v_fragPos, viewDir, v_fragPosLightSpace_2);
        }
    }
#endif

    result *= color;

    gl_FragColor = vec4(result, 1.0);
    gl_FragColor = toGamma(gl_FragColor);
}

vec3 phongSpecular(vec3 normal, vec3 viewDir, vec3 lightDir, vec3 lightSpecular, float shininess) {
    vec3 reflectDir = reflect(-lightDir, normal);
    return lightSpecular * pow(max(dot(viewDir, reflectDir), 0.0), shininess);
}

vec3 blinnPhongSpecular(vec3 normal, vec3 viewDir, vec3 lightDir, vec3 lightSpecular, float shininess) {
    vec3 halfwayDir = normalize(lightDir + viewDir);
    return lightSpecular * pow(max(dot(normal, halfwayDir), 0.0), shininess);
}
#define specularTerm phongSpecular
//#define specularTerm blinnPhongSpecular

#if N_DIR_LIGHT > 0
// calculates the color when using a directional light.
vec3 CalcDirLight(int iLight, vec3 normal, vec3 viewDir)
{
    DirLight light = dirLights(iLight);
    vec3 lightDir = normalize(-light.direction);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
//    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material_shininess);
    // combine results
//    vec3 ambient = light.ambient * vec3(texture(material.diffuse, TexCoords));
//    vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse, TexCoords));
//    vec3 specular = light.specular * spec * vec3(texture(material.specular, TexCoords));
    vec3 ambient = light.ambient;
    vec3 diffuse = light.diffuse * diff;
//    vec3 specular = light.specular * spec;
    vec3 specular = specularTerm(normal, viewDir, lightDir, light.specular, material_shininess);
    return (ambient + diffuse + specular);
}
#endif

#if N_POINT_LIGHT > 0
// calculates the color when using a point light.
vec3 CalcPointLight(int iLight, vec3 normal, vec3 fragPos, vec3 viewDir)
{
    PointLight light = pointLights(i);
    vec3 lightDir = normalize(light.position - fragPos);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
//    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material_shininess);
    // attenuation
    float distance = length(light.position - fragPos);
    float attenuation = 1.0 / (light.constant + light._linear * distance + light.quadratic * (distance * distance));
    // combine results
//    vec3 ambient = light.ambient * vec3(texture(material.diffuse, TexCoords));
//    vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse, TexCoords));
//    vec3 specular = light.specular * spec * vec3(texture(material.specular, TexCoords));
    vec3 ambient = light.ambient;
    vec3 diffuse = light.diffuse * diff;
//    vec3 specular = light.specular * spec;
    vec3 specular = specularTerm(normal, viewDir, lightDir, light.specular, material_shininess);
    ambient *= attenuation;
    diffuse *= attenuation;
    specular *= attenuation;
    return (ambient + diffuse + specular);
}
#endif


float ShadowCalculation(vec4 fragPosLightSpace, vec3 normal, SpotLight light, int iLight)
{
    // perform perspective divide
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
//    if (projCoords.x > 1.0 || projCoords.x < 0.0 || projCoords.y > 1.0 || projCoords.y < 0.0)
//        return 0.0;
//    if (projCoords.z > 1.0)
//        return 0.0;

//    vec3 projCoords = fragPosLightSpace.xyz;
    // transform to [0,1] range
    projCoords = projCoords * 0.5 + 0.5;
//    projCoords.x = 1.0 - projCoords.x;
    // get closest depth value from light's perspective (using [0,1] range fragPosLight as coords)
//    float closestDepth = texture2D(s_shadowMap, projCoords.xy).r;
//    float closestDepth = unpackRgbaToFloat(texture2D(s_shadowMap, projCoords.xy));
    float closestDepth;

    if (iLight == 0) {

    closestDepth = unpackRgbaToFloat(texture2D(s_shadowMap_light_0, vec2(projCoords.x, projCoords.y)));
    }
    else if (iLight == 1) {
    closestDepth = unpackRgbaToFloat(texture2D(s_shadowMap_light_1, vec2(projCoords.x, projCoords.y)));

    }
    else {
    closestDepth = unpackRgbaToFloat(texture2D(s_shadowMap_light_2, vec2(projCoords.x, projCoords.y)));
    }
//    closestDepth = unpackRgbaToFloat(texture2D(s_shadowMap_light_1, vec2(projCoords.x, projCoords.y)));
//    float closestDepth = unpackRgbaToFloat(texture2D(s_shadowMap_light_0, vec2(projCoords.x, projCoords.y)));
//    float depth = unpackRgbaToFloat(texture2D(s_shadowMap, v_texcoord0));
    // get depth of current fragment from light's perspective
    float currentDepth = projCoords.z;
    // check whether current frag pos is in shadow
//    float shadow = currentDepth > closestDepth  ? 1.0 : 0.0;
//    float bias = 0.0001;
//    float bias = max(0.00001 * (1.0 - dot(normal, light.direction)), 0.001);
    float bias = 0.0003 * (1.0 - dot(normal, light.direction));
    float shadow = currentDepth - bias > closestDepth  ? 1.0 : 0.0;

    // PCF
//    float shadow = 0.0;
//    textureSize(s_shadowMap, 3);
//    vec2 texelSize = vec2_splat(1.0 / 512.0);
//    const int size = 1;
//    for (int x = -size; x <= size; ++x) {
//        for (int y = -size; y <= size; ++y) {
//            float pcfDepth = unpackRgbaToFloat(texture2D(s_shadowMap, projCoords.xy + vec2(x, y) * texelSize));
//            shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;
//        }
//    }
//    const int side = size * 2 + 1;
//    shadow /= side * side;

    return shadow;
}


#if N_SPOT_LIGHT > 0
// calculates the color when using a spot light.
vec3 CalcSpotLight(int iLight, vec3 normal, vec3 fragPos, vec3 viewDir, vec4 fragPosLightSpace)
{
    SpotLight light = spotLights(iLight);
    vec3 lightDir = normalize(light.position - fragPos);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
//    vec3 reflectDir = reflect(-lightDir, normal);
//    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material_shininess);
//    float spec = specularTerm(normal, viewDir, lightDir, material_shininess);
    // attenuation
    float distance = length(light.position - fragPos);
    float attenuation = 1.0 / (light.constant + light._linear * distance + light.quadratic * (distance * distance));
    // spotlight intensity
    float theta = dot(lightDir, normalize(-light.direction));
    float epsilon = light.cutOff - light.outerCutOff;
    float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
    // combine results
//    vec3 ambient = light.ambient * vec3(texture(material.diffuse, TexCoords));
//    vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse, TexCoords));
//    vec3 specular = light.specular * spec * vec3(texture(material.specular, TexCoords));
    vec3 ambient = light.ambient;
    vec3 diffuse = light.diffuse * diff;
//    vec3 specular = light.specular * spec;
    vec3 specular = specularTerm(normal, viewDir, lightDir, light.specular, material_shininess);

    ambient *= attenuation * intensity;
    diffuse *= attenuation * intensity;
    specular *= attenuation * intensity;

//    float shadow = ShadowCalculation(v_fragPosLightSpace);
    float shadow = ShadowCalculation(fragPosLightSpace, normal, light, iLight);

//    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w * 0.5 + 0.5;
//    if (projCoords.x > 1.0 || projCoords.x < 0.0 || projCoords.y > 1.0 || projCoords.y < 0.0)
//        return vec3(1.0, 0.0, 0.0);
//        continue;
//        0;
//    else
//        return vec3(1.0, 0.0, 0.0);

//    if (fragPosLightSpace.x > 1.0 || fragPosLightSpace.x < -1.0 || fragPosLightSpace.y > 1.0 || fragPosLightSpace.x < -1.0) {
//        return vec3(1.0, 0.0, 0.0);
//    }
//    return vec3_splat(shadow);
//    vec3 lighting = ambient + diffuse + specular;
    vec3 lighting = (ambient + (1.0 - shadow) * (diffuse + specular));
//    return (ambient + diffuse + specular);
    return lighting;
}
#endif

//void main()
//{
//    SpotLight spotLights[N_SPOT_LIGHT];
//    for (int i =0; i <nbSpotLights; ++i) {
//        spotLights[i] = SpotLight(vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 1]),
//                vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 2]),
//                vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 3]),
//                vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 4]),
//                vec3(u_spotLights[i * N_SPOT_LIGHT_VEC4 + 5]),
//                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 1].w,
//                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 2].w,
//                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 3].w,
//                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 4].w,
//                u_spotLights[i * N_SPOT_LIGHT_VEC4 + 5].w);
//    }
////    SpotLight light;
////    light.position = vec3(0.0, 1.0, 0.0);
////    light.direction = vec3(1.0, 0.0, 0.0);
////    light.cutOff = 0.97;
////    light.outerCutOff = 0.95;
//////    light.color = vec3(1.0, 0.0, 0.0);
////    light.ambient = vec3(0.0);
////    light.diffuse = vec3(1.0);
////    light.specular = vec3(1.0);
////    light.constant = 1.0;
////    light._linear = 0.09;
////    light.quadratic = 0.032;
//    SpotLight light = spotLights[0];
////    if (nbSpotLights != 0)
////        return;

//    vec3 color;
//    if (material_hasOpacityTexture > -0.5) {
//        color = texture2D(s_opacity, v_texcoord0).xyz;
//        if (color.r < 0.1) {
//            discard;
//        }
//    }

////    vec3 result = vec3_splat(0.0); // vec3_splat != vec3 for direct3D


////    vec3 matColor = toLinear(texture2D(s_diffuse, v_texcoord0) ).rgb;
////    vec3 color = texture2D(s_diffuse, v_texcoord0).rgb;
//    if (material_hasDiffuseTexture > -0.5) {
////                color = texture2D(s_diffuse, v_texcoord0).xyz;
//        color = toLinear(texture2D(s_diffuse, v_texcoord0)).xyz;
//    } else {
//        color = material_diffuse;
//    }


//    // ambient
//    vec3 ambient = light.ambient * color;

//    // diffuse
//    vec3 norm = normalize(v_normal);
//    vec3 lightDir = normalize(light.position - v_fragPos);
//    float diff = max(dot(norm, lightDir), 0.0);
//    vec3 diffuse = light.diffuse * diff * color;

//    // specular
//    vec3 viewDir = normalize(viewPos - v_fragPos);
//    vec3 reflectDir = reflect(-lightDir, norm);
//    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material_shininess);
//    vec3 specular = light.specular * spec * color;

//    // spotlight (soft edges)
//    float theta = dot(lightDir, normalize(-light.direction));
//    float epsilon = (light.cutOff - light.outerCutOff);
//    float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
//    diffuse  *= intensity;
//    specular *= intensity;

//    // attenuation
//    float distance    = length(light.position - v_fragPos);
//    float attenuation = 1.0 / (light.constant + light._linear * distance + light.quadratic * (distance * distance));
//    ambient  *= attenuation;
//    diffuse   *= attenuation;
//    specular *= attenuation;

//    vec3 result = ambient + diffuse + specular;
////    vec3 result = ambient;
////    FragColor = vec4(result, 1.0);
//    gl_FragColor = vec4(result, 1.0);
//    gl_FragColor = toGamma(gl_FragColor);
//}



//vec3 calculateLambertDiffuse(vec3 normal, vec3 light_dir, vec3 diffuse_color)
//{
//    return max(dot(normal, light_dir), 0.0) * diffuse_color;
//}

//vec3 calculateBlinnSpecular(vec3 normal, vec3 view_dir, vec3 light_dir, vec3 specular_color, float shininess)
//{
//    vec3 half_dir = normalize(light_dir + view_dir);
//    float angle = max(dot(half_dir, normal), 0.0);
//    float strength = pow(angle, shininess);
//    return strength * specular_color;
//}

//vec3 calculateSingleLightShading(DirLight dirLight, vec3 color, vec3 normal, vec3 view_dir)
//{
////    dirLight.dir = vec4(0.0, -1.0, 0.5, 0.0);
////    dirLight.color = vec4(1.0, 1.0, 1.0, 0.0);

//    vec3 light_dir = normalize(-dirLight.dir);
//    //    vec3 diffuse = dir_light.intensity * calculateLambertDiffuse(normal, light_dir, material.diffuse);
//    //    vec3 specular = dir_light.intensity * calculateBlinnSpecular(normal, view_dir, light_dir, material.specular, material.shininess);

//    vec3 diffuse = dirLight.color * calculateLambertDiffuse(normal, light_dir, color);
//    vec3 specular = dirLight.color * calculateBlinnSpecular(normal, view_dir, light_dir, color, material_shininess);
//    //    return diffuse;
//    //    return specular;

//    return diffuse + specular;
//}

//void main()
//{
//    vec3 color;
//    if (material_hasOpacityTexture > -0.5) {
//        color = texture2D(s_opacity, v_texcoord0).xyz;
//        if (color.r < 0.1) {
//            discard;
//        }
//    }

//    vec3 result = vec3_splat(0.0); // vec3_splat != vec3 for direct3D


////#ifdef BGFX_SHADER_LANGUAGE_PSSL
////    Material material;
////    material.diffuse = u_diffuse;
////    material.specular = u_specular;
////    material.ambient = u_ambient;
////    material.shininess = u_shininess;

////#define dirLights[0]_dir u_dirLights[0].xyz
////#define dirLights[0]_intensity u_dirLights[1].xyz
////    DirLight dirLight = DirLight(dirLight_0_dir, dirLight_0_color);

////    dirLight.dir = vec3(0.0, -1.0, 0.5);
////    dirLight.color = vec3(1.0, 1.0, 1.0);

////    dirLight.dir = u_dir_light_0_dir;
////    dirLight.intensity = u_dir_light_0_color;
////#else
////    Material material = Material(
////        u_diffuse,
////        u_specular,
////        u_ambient,
////        u_shininess);

//    DirLight dirLights[N_DIR_LIGHT];
//    for (int i =0; i <nbDirLights; ++i) {
//        dirLights[i] = DirLight(vec3(u_dirLights[i * 2]), vec3(u_dirLights[i * 2 + 1]));
//    }
////    DirLight dirLight = DirLight(u_dirLights[0], u_dirLights[1]);
////        u_dir_light_0_dir,
////        u_dir_light_0_color);
////#endif

//    // When the triangle is back-facing, the normal direction will be flipped
//    //    vec3 view_dir = normalize(v_view);
//    vec3 view_dir = normalize(viewPos - v_pos);
//    //    vec3 normal = dot(v_normal, view_dir) > 0.0 ? normalize(v_normal) : normalize(- v_normal);
//    vec3 normal = normalize(v_normal);

//    //        vec3 color = texture2D(s_diffuse, v_texcoord0).xyz;
//    if (material_hasDiffuseTexture > -0.5) {
////                color = texture2D(s_diffuse, v_texcoord0).xyz;
//        color = toLinear(texture2D(s_diffuse, v_texcoord0)).xyz;
//    } else {
//        color = material_diffuse;
//    }

//    //        if (color.w < 0.1) {
//    //            discard;
//    //        }
//    //    vec3 color = toLinear(texture2D(s_diffuse, v_texcoord0) ).xyz;


//    result += calculateSingleLightShading(dirLights[0], color, normal, view_dir);
////    result += calculateSingleLightShading(dirLights[1], color, normal, view_dir);

//    //    result += calculateSingleLightShading(DirLight(u_dir_light_1_dir, u_dir_light_1_color), material, normal, view_dir);

//    //    //    vec4 color = texture2D(s_diffuse, v_texcoord0) ;
//    //    //    gl_FragColor.xyz = max(vec3_splat(0.05), lightColor.xyz)*color.xyz;

//    //    result += material.ambient;
//    //        result += color * 0.01;
//    //    result += material.specular;

//    //const float gamma = 2.0;
//    //const float gamma = 5.0;

//    //    const float gamma = 0.1;
//    //    const float exposure = 30.0;
//    //    result = vec3_splat(1.0) - exp(-result * exposure);
//    //    result = pow(result, vec3_splat(1.0 / gamma));

//    //    gl_FragColor.xyz = result;
//    gl_FragColor.xyz = result;
//    gl_FragColor.w = 1.0;
//    gl_FragColor = toGamma(gl_FragColor);
//}
