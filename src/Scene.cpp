#include "Scene.h"

#include "TimerUtil.h"
#include <cassert>
#include <iostream>
#include <stb/stb_image.h>

#include "System.h"
//#include <cassert>

Scene::Scene()
{
    //    Vertex::init();
    m_layout.begin()
        .add(bgfx::Attrib::Position, 3, bgfx::AttribType::Float)
        .add(bgfx::Attrib::Normal, 3, bgfx::AttribType::Float)
        .add(bgfx::Attrib::TexCoord0, 2, bgfx::AttribType::Float)
        // .add(bgfx::Attrib::Color0, 4, bgfx::AttribType::Uint8, true)
        .end();

}

void Scene::addModel(const char* filename)
{
    std::string absoluteFilename(std::string(PROJECT_DIR) + filename);

    tinyobj::attrib_t attrib;
    std::vector<tinyobj::shape_t> shapes;
    std::vector<tinyobj::material_t> materials;
    //    std::map<std::string, uint> textures;

    timerutil tm;
    tm.start();

    std::string base_dir = GetBaseDir(absoluteFilename);
    if (base_dir.empty()) {
        base_dir = ".";
    }
#ifdef _WIN32
    base_dir += "\\";
#else
    base_dir += "/";
#endif

    std::string warn;
    std::string err;
    bool ret = tinyobj::LoadObj(&attrib, &shapes, &materials, &warn, &err, absoluteFilename.c_str(),
        base_dir.c_str());
    if (!warn.empty()) {
        std::cout << "[Scene] WARN: " << warn << std::endl;
    }
    if (!err.empty()) {
        std::cerr << "[Scene] " << err << std::endl;
    }

    tm.end();

    if (!ret) {
        std::cerr << "[Scene] Failed to load " << absoluteFilename << std::endl;
        //        throw std::runtime_error("");
        exit(1);
        //        return;
    }

    printf("[Scene] Parsing time: %d [ms]\n", (int)tm.msec());
    m_parsingTime = tm.msec();

    printf("[Scene] # of vertices  = %d\n", (int)(attrib.vertices.size()) / 3);
    printf("[Scene] # of normals   = %d\n", (int)(attrib.normals.size()) / 3);
    printf("[Scene] # of texcoords = %d\n", (int)(attrib.texcoords.size()) / 2);
    printf("[Scene] # of materials = %d\n", (int)materials.size());
    printf("[Scene] # of shapes    = %d\n", (int)shapes.size());

    // Append `default` material
    materials.push_back(tinyobj::material_t());

    //    for (size_t i = 0; i < materials.size(); i++) {
    //        printf("material[%d].diffuse_texname = %s\n", int(i),
    //            materials[i].diffuse_texname.c_str());
    //    }

    // Load diffuse textures
    //    {
    //    for (const tinyobj::material_t& material : materials) {
    m_materials.reserve(materials.size());
    m_textures.reserve(materials.size());

//    timerutil tm;
    tm.start();

    for (size_t i = 0; i < materials.size(); i++) {
        const tinyobj::material_t& material = materials[i];

        std::cout << "[Scene] Load material[" << i << "] " << material.name << std::endl;
        //        printf("material[%d].difname = %s\n", int(i),
        //            material.name.c_str());
        //        m_materials.push_back(Material(material, m_textures, base_dir));
        m_materials.emplace_back(material, m_textures, base_dir);
    }
    //    for (const auto & texture : m_textures) {
    ////        const auto texture = pair.second;
    //        assert(bgfx::isValid(texture.textureHandle()));
    //    }
    assert(materials.size() == m_materials.size());

    tm.end();
    m_loadingMaterialsTime = tm.msec();

    tm.start();


    m_objects.reserve(shapes.size());
    for (size_t i = 0; i < shapes.size(); ++i) {
        const tinyobj::shape_t& shape = shapes[i];
        //        m_objects.push_back(Object(shape, attrib, materials, i));
        m_objects.emplace_back(shape, attrib, materials, i, m_layout);
    }

    tm.end();
    m_loadingObjectsTime = tm.msec();


    m_uDiffuse = bgfx::createUniform("u_diffuseColor", bgfx::UniformType::Vec4);
    m_samplerDiffuse = bgfx::createUniform("s_texColor", bgfx::UniformType::Sampler);
    m_uHasDiffuseTexture = bgfx::createUniform("u_hasDiffuseTexture", bgfx::UniformType::Vec4);

    std::cout << "[Scene] Parsing time: " << m_parsingTime << " [ms]" << std::endl;
    std::cout << "[Scene] Loading materials time: " << m_loadingMaterialsTime << " [ms]" << std::endl;
    std::cout << "[Scene] Loading objects time: " << m_loadingObjectsTime << " [ms]" << std::endl;
}

void Scene::submit(bgfx::ViewId id, bgfx::ProgramHandle program, const float* mtx, uint64_t state)
{
        for (const Object & object : m_objects) {
//    const uint nbObjects = m_objects.size();
//            bgfx::setTransform(mtx);
//            bgfx::setState(state);
//        for (int i =0; i <nbObjects; ++i) {
//            const Object & object = m_objects[i];

            const int iMaterial = object.iMaterial();
            assert(0 <= iMaterial && iMaterial < m_materials.size());
            const Material & material = m_materials[object.iMaterial()];

            bgfx::setTransform(mtx);
            bgfx::setState(state);


            bgfx::setUniform(m_uDiffuse, material.diffuse());

//    		if (group.m_texture != nullptr) {
            const int iTexDiffuse = material.iTexDiffuse();
                if (iTexDiffuse >= 0) {

                    assert(iTexDiffuse < m_textures.size());
                    const Texture & texture = m_textures[material.iTexDiffuse()];
//                }
                // uint32_t flags = UINT32_MAX;
                // uint32_t flags = BGFX_TEXTURE_USE_DEFAULT;

                // const uint64_t textureFlags =
                    // 0 | BGFX_CAPS_FORMAT_TEXTURE_MIP_AUTOGEN;
                // const uint64_t textureFlags = 0 | BGFX_TEXTURE_NONE;
                // const uint64_t samplerFlags = 0 | BGFX_SAMPLER_NONE;
                bgfx::setTexture(0, m_samplerDiffuse,
                                 texture.textureHandle());
                                //  textureFlags | samplerFlags);

//                 bgfx::setUniform(m_uHasDiffuseTexture, (void*)true);
                float temp[1] = {1.0};
                 bgfx::setUniform(m_uHasDiffuseTexture, temp);
//                 bgfx::setUniform(m_uHasDiffuseTexture);
            }

            // else {
            // bgfx::setTexture(0, group.m_texture->m_sampler, bgfx::Tex)
            // }

            // }
            // else {
            //     continue;
            // }

//    		bgfx::setIndexBuffer(group.m_ibh);

            bgfx::setVertexBuffer(0, object.vbh());

//             bgfx::submit(id, program, 0, i != nbObjects - 1);
            bgfx::submit(id, program);
        }
}

void Scene::clear()
{
    //    bgfx::destroy(m_layout);
    //    m_objects.clear();
    //    m_materials.clear();
    //    for (const auto & texture : m_textures) {
    ////        const auto & texture = pair.second;
    //        assert(bgfx::isValid(texture.textureHandle()));
    //    }
    m_textures.clear(); // bgfx::TextureHandle
    m_objects.clear(); // bgfx::VertexBufferHandle
    //    m_materials.clear();
}

size_t Scene::nbVertices() const
{
    size_t nbVertices = 0;
    for (const Object & object : m_objects) {
        nbVertices += object.nbVertices();
    }

    return nbVertices;
}

size_t Scene::nbTriangles() const
{
    size_t nbTriangles = 0;
    for (const Object & object : m_objects) {
        nbTriangles += object.nbTriangles();
    }

    return nbTriangles;
}

size_t Scene::nbObjects() const
{
    return m_objects.size();
}

size_t Scene::texturesSize() const
{
    size_t texturesSize = 0;
    for (const Texture & texture : m_textures) {
        texturesSize += texture.textureSize();
    }

    return texturesSize;
}
